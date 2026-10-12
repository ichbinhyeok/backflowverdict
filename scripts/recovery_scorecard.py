"""Create a review queue or compare settled GSC exports with the recovery baseline.

Examples (run from repository root):
  python scripts/recovery_scorecard.py --refresh-queue build/ops/source-refresh-queue.csv
  python scripts/recovery_scorecard.py --dates dates.csv --pages pages.csv --as-of 2026-12-12
CSV columns: dates date,clicks,impressions; pages url,clicks,impressions.
The page export must cover exactly the latest 28 dates, use the same web-search filters,
and include zero-click rows. GA4 conversion/revenue exports are reviewed separately.
"""
import argparse
import csv
import datetime as dt
import json
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]

def read_csv(path):
    with Path(path).open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--dates')
    parser.add_argument('--pages')
    parser.add_argument('--as-of', default=dt.date.today().isoformat())
    parser.add_argument('--refresh-queue')
    args = parser.parse_args()
    as_of = dt.date.fromisoformat(args.as_of)
    experiment = json.loads((ROOT/'ops/search-recovery-experiment.json').read_text(encoding='utf-8'))
    if args.refresh_queue:
        queue = []
        for file in (ROOT/'data/utilities').rglob('*.json'):
            record = json.loads(file.read_text(encoding='utf-8-sig'))
            verified = dt.date.fromisoformat(record['lastVerified'])
            age = (as_of-verified).days
            queue.append(dict(utility=record['utilityName'],path=str(file.relative_to(ROOT)),published=record.get('pageStatus'),lastVerified=verified.isoformat(),ageDays=age,overdueDays=max(0,age-record.get('staleAfterDays',45)),primarySource=(record.get('officialProgramUrls') or [''])[0],action='Read current official requirements and reporting route; update date only after semantic review'))
        queue.sort(key=lambda r:(-r['overdueDays'],r['utility']))
        target=Path(args.refresh_queue);target.parent.mkdir(parents=True,exist_ok=True)
        with target.open('w',encoding='utf-8',newline='') as f:
            writer=csv.DictWriter(f,fieldnames=list(queue[0]));writer.writeheader();writer.writerows(queue)
        print(json.dumps(dict(queue=str(target),records=len(queue),overdue=sum(r['overdueDays']>0 for r in queue))))
    if bool(args.dates) != bool(args.pages):
        parser.error('--dates and --pages must be supplied together')
    if not args.dates:
        return
    days=read_csv(args.dates)
    dated={dt.date.fromisoformat(row['date']):row for row in days}
    if len(dated)!=len(days):
        raise ValueError('Duplicate dates in export')
    end=max(dated);start=end-dt.timedelta(days=27)
    expected={start+dt.timedelta(days=n) for n in range(28)}
    if not expected.issubset(dated):
        raise ValueError('Need 28 complete settled dates; do not fill missing data with zeros')
    totals={key:sum(float(dated[day][key]) for day in expected) for key in ('clicks','impressions')}
    page_rows=read_csv(args.pages)
    paths=[urlparse(row['url']).path for row in page_rows]
    if len(paths)!=len(set(paths)):
        raise ValueError('Page export must be aggregated by URL without duplicate paths')
    focus=[r for r in page_rows if urlparse(r['url']).path in experiment['focusPaths']]
    focus_clicks=sum(float(r['clicks']) for r in focus)
    deployment=experiment.get('deploymentDate')
    elapsed=(end-dt.date.fromisoformat(deployment)).days if deployment else None
    result=dict(range=dict(start=start.isoformat(),end=end.isoformat()),site=totals,focusPageClicks=focus_clicks,focusPageImpressions=sum(float(r['impressions']) for r in focus),baseline=experiment['baseline'],daysSinceDeploymentAtDataCutoff=elapsed,commercialAssessment='Requires GA4 events plus accepted inquiry and collected revenue evidence; not inferred from GSC.',caveat='Page export range/filter agreement is operator-verified; Search Console privacy and aggregation can differ.')
    eligible=[g for g in experiment['gates'] if elapsed is not None and elapsed>=g['day']]
    result['gate']=dict(**eligible[-1],siteClickGateMet=totals['clicks']>=eligible[-1]['siteClicks28Days'],focusClickGateMet=focus_clicks>=eligible[-1]['focusClicks28Days']) if eligible else 'Not due / deployment not recorded'
    print(json.dumps(result,indent=2))

if __name__ == '__main__':
    main()
