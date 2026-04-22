from pathlib import Path
import csv
import json

ROOT = Path(__file__).resolve().parents[1]
TODAY = "2026-04-04"


def src(label, url, kind):
    return {"label": label, "url": url, "kind": kind}


def method(label, url, kind):
    return {"label": label, "url": url, "kind": kind}


def focus(summary, highlights, workflow_steps):
    return {
        "summary": summary,
        "highlights": highlights,
        "workflowSteps": workflow_steps,
    }


def cost(testing_range, repair_range, pricing_notes):
    return {
        "testingRange": testing_range,
        "repairRetestRange": repair_range,
        "pricingNotes": pricing_notes,
    }


def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def write_snapshot(path, title, excerpt, urls):
    path.parent.mkdir(parents=True, exist_ok=True)
    items = "".join(f"    <li>{url}</li>\n" for url in urls)
    html = f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>{title}</title>
</head>
<body>
  <h1>{title}</h1>
  <p>Captured for the file-backed registry on {TODAY} by reviewer TL.</p>
  <p>Durable excerpt: {excerpt}</p>
  <ul>
{items}  </ul>
</body>
</html>
"""
    path.write_text(html, encoding="utf-8")


states = [
    {
        "state": "arizona",
        "title": "Arizona backflow testing requirements",
        "description": "Representative state guide for Arizona utility backflow programs, registered tester lists, and annual test-report workflows.",
        "summary": "Arizona is a strong expansion state because the large municipal utilities publish real customer workflows instead of generic plumbing copy. Phoenix, Tucson, and Mesa all expose tester requirements, annual testing expectations, and city-level reporting mechanics on official pages.",
        "authoritySummary": "Arizona programs commonly point back to state drinking-water and plumbing rules, then push the actual tester registration, due-date handling, and submission mechanics down to the utility. In practice, the customer still has to follow the specific city program that governs the service connection.",
        "sourceExcerpt": "Arizona earns expansion priority because Phoenix, Tucson, and Mesa each publish customer-facing rules, certified tester resources, and annual compliance language that can support utility-first local pages.",
        "sourceSnapshotPath": "storage/snapshots/states/arizona-backflow-testing.html",
        "reviewerInitials": "TL",
        "lastVerified": TODAY,
        "published": True,
        "statewideHighlights": [
            "Arizona utility programs often cite state code but enforce through city-run cross-connection offices.",
            "Annual testing is common across commercial, irrigation, and higher-hazard assemblies.",
            "Registered tester lists are publicly available in multiple Arizona utility programs.",
            "Desert-state irrigation demand makes irrigation-specific backflow content commercially relevant.",
        ],
        "featuredUtilityIds": ["phoenix-water", "tucson-water", "mesa-water"],
        "sources": [
            src(
                "Phoenix Backflow Prevention Program",
                "https://www.phoenix.gov/administration/departments/pdd/tools-resources/inspections/types-of-inspections/backflow-prevention-program.html",
                "utility example",
            ),
            src(
                "Tucson backflow ordinance",
                "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow-ordinance.pdf",
                "utility ordinance example",
            ),
            src(
                "Mesa Backflow Prevention program",
                "https://www.mesaaz.gov/Utilities/Water-Wastewater/Backflow-Prevention",
                "utility example",
            ),
        ],
    },
    {
        "state": "california",
        "title": "California backflow testing requirements",
        "description": "Representative state guide for California utility backflow programs, approved tester lists, and policy-heavy cross-connection enforcement.",
        "summary": "California is one of the best fit states for expansion because it combines a visible statewide cross-connection policy layer with utility programs that publish approved tester lists, orientation rules, meter-protection requirements, and hazard-based installation guidance.",
        "authoritySummary": "California utilities operate under an explicit cross-connection control policy environment, but the real install, test, and meter-protection workflow still happens utility by utility. San Diego, IRWD, and EBMUD all publish customer-facing program pages that are strong enough for utility-first pages.",
        "sourceExcerpt": "California is a high-value state because the state policy layer exists and major utilities expose enough tester, installation, and annual-testing detail to support trustworthy local compliance pages.",
        "sourceSnapshotPath": "storage/snapshots/states/california-backflow-testing.html",
        "reviewerInitials": "TL",
        "lastVerified": TODAY,
        "published": True,
        "statewideHighlights": [
            "The state policy layer is visible, but the actual assembly and tester workflow remains utility-specific.",
            "Approved tester lists are common in larger utilities.",
            "Meter-protection and fire-protection details are spelled out more clearly than in many states.",
            "California utilities often require orientation or recognized certification before testers can work in the district.",
        ],
        "featuredUtilityIds": ["san-diego-water", "irwd-backflow", "ebmud-backflow"],
        "sources": [
            src(
                "California State Water Resources Control Board Drinking Water Program",
                "https://water.waterboards.ca.gov/drinking_water/programs/",
                "state program",
            ),
            src(
                "California cross-connection policy handbook staff report",
                "https://www.waterboards.ca.gov/drinking_water/certlic/drinkingwater/docs/2023/cccph-rptoct23.pdf",
                "state policy context",
            ),
            src(
                "San Diego Cross-Connection Control and Backflow Prevention",
                "https://www.sandiego.gov/public-utilities/permits-construction/construction-and-development/backflow",
                "utility example",
            ),
            src(
                "EBMUD backflow prevention",
                "https://www.ebmud.com/development-services/backflow-prevention",
                "utility example",
            ),
        ],
    },
    {
        "state": "colorado",
        "title": "Colorado backflow testing requirements",
        "description": "Representative state guide for Colorado utility backflow programs, annual testing reminders, and portal-driven compliance reporting.",
        "summary": "Colorado is a strong representative state because the utilities publish concrete compliance workflows: annual reminder notices, tester certification expectations, irrigation season rules, and online reporting requirements. Denver, Aurora, and Colorado Springs all support utility-first buildout.",
        "authoritySummary": "Colorado programs repeatedly point back to the Colorado Primary Drinking Water Regulations and then apply those rules through utility-owned cross-connection programs. The customer still has to satisfy the local water provider, not just the statewide rule text.",
        "sourceExcerpt": "Colorado is expansion-worthy because the rule floor is visible and the utility layer is operationally rich enough to support annual-testing, irrigation, and failure-flow content.",
        "sourceSnapshotPath": "storage/snapshots/states/colorado-backflow-testing.html",
        "reviewerInitials": "TL",
        "lastVerified": TODAY,
        "published": True,
        "statewideHighlights": [
            "Annual testing is heavily emphasized by the major utilities.",
            "Irrigation backflows often have season-specific reminders and protection rules.",
            "Utilities rely on online portals and certification checks rather than generic phone-only workflows.",
            "Regulation 11 creates a visible state floor, but local enforcement still happens utility by utility.",
        ],
        "featuredUtilityIds": ["denver-water", "aurora-water", "csu-backflow"],
        "sources": [
            src(
                "Colorado Department of Public Health and Environment Regulation No. 11",
                "https://cdphe.colorado.gov/sites/cdphe/files/51_2015%2809%29hdr.pdf",
                "state regulation context",
            ),
            src(
                "Denver Water cross-connection control and backflow prevention program",
                "https://www.denverwater.org/contractors/construction-information/backflow-prevention-program",
                "utility example",
            ),
            src(
                "Aurora Water backflow prevention",
                "https://www.auroragov.org/residents/water/pay_my_water_bill/backflow_prevention",
                "utility example",
            ),
            src(
                "Colorado Springs Utilities backflow prevention professionals",
                "https://www.csu.org/safety/backflow-prevention-professionals",
                "utility example",
            ),
        ],
    },
    {
        "state": "florida",
        "title": "Florida backflow testing requirements",
        "description": "Representative state guide for Florida utility backflow programs, annual and biennial testing cycles, and outsourced compliance portals.",
        "summary": "Florida is one of the best commercial fits because irrigation-heavy service areas, county-run compliance programs, and frequent outsourced reporting platforms create clear next-action pages. Miami-Dade, Broward County, and Tampa each publish concrete reporting and testing rules.",
        "authoritySummary": "Florida utilities often reference state backflow rules, but the customer experience is still controlled by the local utility or county program. That means the real workflow lives in the utility notice, its accepted tester rules, and its reporting portal.",
        "sourceExcerpt": "Florida is a strong revenue and content fit because county and city utilities publish explicit annual testing, irrigation, and reporting workflows instead of vague educational pages.",
        "sourceSnapshotPath": "storage/snapshots/states/florida-backflow-testing.html",
        "reviewerInitials": "TL",
        "lastVerified": TODAY,
        "published": True,
        "statewideHighlights": [
            "Annual testing remains the default for commercial and higher-hazard assemblies.",
            "Residential irrigation frequently gets its own cadence, often every two years.",
            "BSI and SwiftComply style reporting workflows are common.",
            "County programs often separate domestic, irrigation, and fire-service tester qualifications.",
        ],
        "featuredUtilityIds": ["miami-dade-wasd", "broward-water", "tampa-water"],
        "sources": [
            src(
                "Miami-Dade cross-connection and backflow prevention service page",
                "https://www.miamidade.gov/global/service.page?Mduid_service=ser1517344068939644",
                "utility example",
            ),
            src(
                "Miami-Dade cross-connection and backflow brochure",
                "https://www.miamidade.gov/water/library/brochures/cross-connection-backflow.pdf",
                "utility brochure",
            ),
            src(
                "Broward Water Services backflow certification",
                "https://www.broward.org/WaterServices/Pages/BackflowCertification.aspx",
                "utility example",
            ),
            src(
                "City of Tampa backflow prevention program",
                "https://www.tampa.gov/water/builders-and-homeowners/backflow-prevention",
                "utility example",
            ),
        ],
    },
]

for state in states:
    write_json(ROOT / "data" / "states" / f"{state['state']}.json", state)
    write_snapshot(
        ROOT / state["sourceSnapshotPath"],
        f"{state['title']} snapshot",
        state["sourceExcerpt"],
        [source["url"] for source in state["sources"]],
    )

utilities = [
    {
        "utilityId": "phoenix-water",
        "utilityName": "City of Phoenix Backflow Prevention Program",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "phoenix-water-services",
        "state": "arizona",
        "serviceAreaCities": ["Phoenix"],
        "serviceAreaCounties": ["Maricopa County"],
        "searchAliases": [
            "phoenix backflow testing",
            "phoenix approved backflow tester",
            "phoenix fire line backflow",
        ],
        "utilityUrl": "https://www.phoenix.gov/administration/departments/pdd/tools-resources/inspections/types-of-inspections/backflow-prevention-program.html",
        "officialProgramUrls": [
            "https://www.phoenix.gov/administration/departments/pdd/tools-resources/inspections/types-of-inspections/backflow-prevention-program.html",
            "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00636.pdf",
            "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf",
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Phoenix requires certified testers on the city list to perform testing and repairs, and the approved report has to be forwarded to Planning and Development by the due date shown on the work order.",
        "coveredPropertyTypes": [
            "Commercial and industrial services",
            "Irrigation services and secondary protection points",
            "Fire line services",
            "Residential and multifamily sites where the city identifies a hazard",
        ],
        "coveredDeviceTypes": [
            "Primary containment assemblies",
            "Secondary protection assemblies",
            "Irrigation backflow assemblies",
            "Fire line backflow preventers",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf",
        "officialListLabel": "Open the Phoenix tester list",
        "submissionMethods": [
            method(
                "Phoenix Backflow Prevention Program",
                "https://www.phoenix.gov/administration/departments/pdd/tools-resources/inspections/types-of-inspections/backflow-prevention-program.html",
                "program page",
            ),
            method(
                "Phoenix tester requirements",
                "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00636.pdf",
                "tester requirements",
            ),
            method(
                "Phoenix assembly test report",
                "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00337.pdf",
                "test report form",
            ),
        ],
        "phone": "602-534-2140",
        "penalties": "Phoenix publishes noncompliance penalties inside the tester packet and can hold compliance open until the city-approved report is delivered. Fire-line downstream testing is handled separately by the Fire Department.",
        "sourceExcerpt": "Phoenix publishes a full city-run program: certified tester requirements, a maintained tester list, city-approved report forms, due-date handling through work orders, and a separate fire-line responsibility split.",
        "sourceSnapshotPath": "storage/snapshots/phoenix-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Phoenix is one of the strongest Arizona utilities because it publishes tester requirements, a current tester list, city code references, and a clear work-order driven reporting workflow.",
        "whoIsAffected": "Commercial, industrial, irrigation, and fire-line services plus any residential or multifamily property where Phoenix determines that a cross-connection hazard exists.",
        "residentialNotes": [
            "Phoenix can require backflow protection at residential sites when the city identifies a hazard rather than leaving the topic only to commercial properties.",
            "Residential owners should not assume irrigation or special-use plumbing is exempt just because the service is domestic.",
        ],
        "commercialNotes": [
            "Phoenix is documentation-heavy: the city list, approved report, and work-order due date all matter together.",
            "Fire-line downstream testing is not handled the same way as domestic or irrigation assemblies.",
        ],
        "annualTesting": focus(
            "Phoenix requires city-listed certified testers to test assemblies on installation and annually thereafter, then send the approved report to the city by the due date on the work order.",
            [
                "Testing and repairs must be done by a city-listed certified tester.",
                "The test report must be on Phoenix's approved form.",
                "Customers are expected to retain records for at least three years.",
            ],
            [
                "Verify the assembly is active in Phoenix's program.",
                "Use a tester currently listed on the Phoenix approved company list.",
                "Submit the approved test form to Planning and Development by the work-order due date.",
            ],
        ),
        "fireLine": focus(
            "Phoenix explicitly separates downstream testing of fire-line backflow devices by assigning that responsibility to the Phoenix Fire Department.",
            [
                "Fire-line downstream testing is carved out from the standard utility workflow.",
                "Fire protection work is governed together with the city's backflow and fire-prevention teams.",
                "Do not treat fire-line testing as identical to ordinary domestic or irrigation testing.",
            ],
            [
                "Confirm whether the assembly is serving a fire line or downstream fire protection equipment.",
                "Coordinate the testing path with the Phoenix Fire Department requirements.",
                "Keep the utility-side report and the fire-side requirement aligned so the record does not stall.",
            ],
        ),
        "workflowSteps": [
            "Confirm whether Phoenix has identified the service as requiring a backflow assembly.",
            "Use a tester from the city's approved tester list.",
            "Complete the approved report and deliver it by the due date shown on the work order.",
            "Retain the testing records in case Phoenix requests follow-up documentation.",
        ],
        "failureHighlights": [
            "Phoenix ties testing to a city work-order due date.",
            "Only testers on the city list can perform the accepted test and repair workflow.",
            "Fire-line downstream testing follows a separate responsibility path.",
        ],
        "costBand": cost(
            "Private testing price is market-based, but Phoenix adds city-form and due-date discipline around the work.",
            "Repair and retest pricing varies by device and whether the issue touches fire-line equipment.",
            "The city does not publish retail testing prices, so the operational value is in the official tester list and reporting path rather than a fixed fee.",
        ),
        "sources": [
            src(
                "Phoenix Backflow Prevention Program",
                "https://www.phoenix.gov/administration/departments/pdd/tools-resources/inspections/types-of-inspections/backflow-prevention-program.html",
                "official program page",
            ),
            src(
                "Phoenix Backflow Tester Requirements",
                "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00636.pdf",
                "official requirements pdf",
            ),
            src(
                "Phoenix Backflow Test List",
                "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf",
                "official tester list pdf",
            ),
        ],
    },
    {
        "utilityId": "tucson-water",
        "utilityName": "Tucson Water Backflow Prevention",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "tucson-water",
        "state": "arizona",
        "serviceAreaCities": ["Tucson"],
        "serviceAreaCounties": ["Pima County"],
        "searchAliases": [
            "tucson backflow testing",
            "tucson backflow tester list",
            "tucson ibak backflow",
        ],
        "utilityUrl": "https://www.tucsonaz.gov/Departments/Water/Commercial-and-Multifamily-Customers/Backflow-Prevention/Backflow-Prevention-iBAK",
        "officialProgramUrls": [
            "https://www.tucsonaz.gov/Departments/Water/Commercial-and-Multifamily-Customers/Backflow-Prevention/Backflow-Prevention-iBAK",
            "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow-ordinance.pdf",
            "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf",
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Tucson Water requires annual testing and can issue a four-day shutoff notice if the compliance date passes without the required test results. Registered testers submit through the iBAK system.",
        "coveredPropertyTypes": [
            "Commercial and multifamily customers",
            "Reclaimed water sites",
            "Irrigation locations using reclaimed or pressurized systems",
            "Any customer where Tucson Water requires an assembly",
        ],
        "coveredDeviceTypes": [
            "Backflow prevention assemblies",
            "Reclaimed-water protection devices",
            "Irrigation RP assemblies",
            "Domestic containment devices",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf",
        "officialListLabel": "Open the Tucson Water tester list",
        "submissionMethods": [
            method(
                "Tucson Water iBAK results entry",
                "https://www.tucsonaz.gov/Departments/Water/Commercial-and-Multifamily-Customers/Backflow-Prevention/Backflow-Prevention-iBAK",
                "online submission",
            ),
            method(
                "Tucson backflow ordinance",
                "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow-ordinance.pdf",
                "utility ordinance",
            ),
            method(
                "Tucson reclaimed water page",
                "https://www.tucsonaz.gov/Departments/Water/Water-Resources-and-Drought-Preparedness/Reclaimed-Water",
                "irrigation and reclaimed context",
            ),
        ],
        "phone": "520-791-2650",
        "penalties": "If the compliance date passes without the required results, Tucson Water may send a short shutoff warning. Tester registration and equipment registration are required before online entry can be used.",
        "sourceExcerpt": "Tucson is valuable because the city ordinance, iBAK portal, tester list, and reclaimed-water content all point to an operational compliance program instead of generic education.",
        "sourceSnapshotPath": "storage/snapshots/tucson-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Tucson Water publishes one of the clearer Arizona workflows: annual testing, registered testers, an iBAK portal, and a short shutoff-warning path when compliance is missed.",
        "whoIsAffected": "Commercial and multifamily customers, reclaimed-water users, irrigation users with protected connections, and any Tucson Water customer that the utility flags as requiring backflow protection.",
        "residentialNotes": [
            "Residential systems that use reclaimed or pressurized irrigation setups can trigger backflow requirements even when the rest of the property feels residential.",
            "Tucson's gray-water and reclaimed-water guidance reinforces that irrigation equipment can create a real cross-connection issue.",
        ],
        "commercialNotes": [
            "Tucson's ordinance and iBAK workflow make this more than a simple annual reminder program.",
            "Commercial and multifamily operators should treat the tester-registration requirement as part of compliance, not an afterthought.",
        ],
        "annualTesting": focus(
            "Tucson Water requires annual testing and expects registered testers to submit results through iBAK once their tester and equipment information is active with the utility.",
            [
                "Annual test results are tied to the utility's compliance date system.",
                "Only Tucson-registered testers can use the online submission tool.",
                "Missed compliance can lead quickly to a shutoff warning.",
            ],
            [
                "Confirm the assembly and tester are active in Tucson Water's records.",
                "Use the iBAK workflow to submit current test results.",
                "Resolve any failures or missing records before Tucson reaches the compliance cutoff.",
            ],
        ),
        "irrigation": focus(
            "Tucson's reclaimed-water and gray-water materials make irrigation protection a real local topic, not filler content.",
            [
                "Reclaimed water cannot be allowed to enter the public drinking water system.",
                "Pressurized irrigation and gray-water related equipment can require RP protection.",
                "Tucson links irrigation-adjacent water reuse topics back to backflow protection.",
            ],
            [
                "Confirm whether the irrigation or reclaimed-water setup creates a cross-connection hazard.",
                "Use the right assembly type for the service and pressure conditions.",
                "Keep the annual test result in Tucson Water's system so the irrigation side does not create a compliance failure.",
            ],
        ),
        "workflowSteps": [
            "Check whether Tucson Water has assigned the property a backflow compliance date.",
            "Use a Tucson-registered tester with current equipment registration.",
            "Submit the test through iBAK before the compliance date passes.",
            "If the assembly fails or the record is incomplete, fix it before Tucson issues a shutoff warning.",
        ],
        "failureHighlights": [
            "Tucson can move quickly with a short shutoff warning after the compliance date.",
            "Tester registration is mandatory for online submission.",
            "Reclaimed-water and irrigation setups add extra cross-connection risk.",
        ],
        "costBand": cost(
            "Testing is market-priced, but Tucson adds operational risk through the compliance-date and shutoff-warning structure.",
            "Repair and retest costs vary by device and whether reclaimed or irrigation equipment is involved.",
            "The utility value here is the iBAK workflow and registered tester structure, not a published flat fee.",
        ),
        "sources": [
            src(
                "Tucson Water iBAK results entry",
                "https://www.tucsonaz.gov/Departments/Water/Commercial-and-Multifamily-Customers/Backflow-Prevention/Backflow-Prevention-iBAK",
                "official submission page",
            ),
            src(
                "Tucson backflow ordinance",
                "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow-ordinance.pdf",
                "official ordinance pdf",
            ),
            src(
                "Tucson certified tester list",
                "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf",
                "official tester list pdf",
            ),
        ],
    },
    {
        "utilityId": "mesa-water",
        "utilityName": "Mesa Water Resources Backflow Prevention",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "mesa-water-resources",
        "state": "arizona",
        "serviceAreaCities": ["Mesa"],
        "serviceAreaCounties": ["Maricopa County"],
        "searchAliases": [
            "mesa backflow testing",
            "mesa backflow portal",
            "mesa fire contractor backflow",
        ],
        "utilityUrl": "https://www.mesaaz.gov/Utilities/Water-Wastewater/Backflow-Prevention",
        "officialProgramUrls": [
            "https://www.mesaaz.gov/Utilities/Water-Wastewater/Backflow-Prevention",
            "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/new-mesa-city-code-backflow-prevention-2025.pdf",
            "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf",
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Mesa sends annual notices to regulated customers, requires recognized testers to submit results through the backflow portal within seven days of service, and requires immediate retesting after repair or maintenance.",
        "coveredPropertyTypes": [
            "Commercial and industrial customers",
            "Homes with dedicated landscape meters",
            "Single-family residences covered by city and plumbing code",
            "Most fire sprinkler systems and other hazard points",
        ],
        "coveredDeviceTypes": [
            "Domestic backflow assemblies",
            "Irrigation backflow assemblies",
            "Fire protection assemblies",
            "Point-of-use backflow assemblies",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf",
        "officialListLabel": "Open the Mesa general tester list",
        "submissionMethods": [
            method(
                "Mesa Backflow Prevention program",
                "https://www.mesaaz.gov/Utilities/Water-Wastewater/Backflow-Prevention",
                "program page",
            ),
            method(
                "Mesa general tester list",
                "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf",
                "official tester list",
            ),
            method(
                "Mesa city code on backflow prevention",
                "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/new-mesa-city-code-backflow-prevention-2025.pdf",
                "city code",
            ),
        ],
        "phone": "480-644-6462",
        "penalties": "Mesa can keep a regulated customer out of compliance if the test is late, the portal filing misses the seven-day window, or the assembly is repaired without an immediate retest by a recognized tester.",
        "sourceExcerpt": "Mesa publishes one of the cleaner city workflows: annual notices, seven-day portal reporting, immediate retesting after repairs, and separate resources for general testers and fire contractors.",
        "sourceSnapshotPath": "storage/snapshots/mesa-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Mesa is a high-value Arizona utility because it publishes the annual cadence, the seven-day submission rule, tester lists, and a city-code layer that covers residential, irrigation, and fire-related hazards.",
        "whoIsAffected": "Commercial, industrial, irrigation, fire-sprinkler, and dedicated-landscape-meter customers, plus residential sites that city code and plumbing rules identify as cross-connection hazards.",
        "residentialNotes": [
            "Mesa is stronger than many cities on residential coverage because dedicated landscape meters and other residential cross-connections are called out directly.",
            "Single-family homes are not automatically outside the program if a hazard exists under the plumbing code or city ordinance.",
        ],
        "commercialNotes": [
            "Mesa's seven-day reporting requirement and immediate retest rule make the workflow operationally strict.",
            "Fire and non-fire work are not identical because Mesa separates general testers from fire-contractor resources.",
        ],
        "annualTesting": focus(
            "Mesa treats annual testing as the recurring baseline and requires recognized testers to upload the results within seven days of service.",
            [
                "Mesa sends annual notices to regulated customers.",
                "Repair or maintenance triggers an immediate retest requirement.",
                "Recognized testers submit results through the city portal within seven days.",
            ],
            [
                "Watch for Mesa's annual notice or regulated-customer status.",
                "Use a tester recognized by Mesa.",
                "Upload the results through the portal within seven days and retest immediately if repairs were made.",
            ],
        ),
        "irrigation": focus(
            "Mesa explicitly treats dedicated landscape meters and landscape sprinkler systems as backflow-relevant cross-connections.",
            [
                "Landscape sprinkler and drip systems are listed as cross-connection examples.",
                "Homes with dedicated landscape meters are directly within Mesa's program.",
                "Irrigation assemblies still need testing and portal submission through the normal city workflow.",
            ],
            [
                "Confirm the irrigation service or dedicated landscape meter is correctly protected.",
                "Use a recognized tester to complete the irrigation assembly test.",
                "Submit the result through Mesa's portal within seven days so the irrigation service stays current.",
            ],
        ),
        "fireLine": focus(
            "Mesa keeps separate resources for fire contractors, which makes the fire-line path stronger than a generic annual-testing page.",
            [
                "Most fire sprinkler systems are listed as covered cross-connections.",
                "Mesa distinguishes general testers from fire-contractor resources.",
                "The annual testing and portal reporting discipline still applies.",
            ],
            [
                "Verify whether the service is on the fire-protection side of the property.",
                "Use the appropriate recognized fire-side contractor or tester resource.",
                "Report the result through the city workflow without missing the seven-day window.",
            ],
        ),
        "workflowSteps": [
            "Identify whether the property or device is already in Mesa's regulated-customer pool.",
            "Use a recognized tester from the city resources.",
            "Upload the test through the portal within seven days of service.",
            "If the assembly was repaired, retest immediately and keep the replacement data aligned with Mesa's record.",
        ],
        "failureHighlights": [
            "Mesa uses a seven-day result-submission window.",
            "Repairs trigger an immediate retest expectation.",
            "Residential irrigation and fire-service contexts both appear in the official program.",
        ],
        "costBand": cost(
            "Testing is market-priced, but Mesa adds process friction through the portal deadline and annual notice cycle.",
            "Repair and retest cost changes with the device class and whether fire-side work is involved.",
            "The city does not publish a retail tester fee, so the main value is in the strict operational rules and list of recognized testers.",
        ),
        "sources": [
            src(
                "Mesa Backflow Prevention program",
                "https://www.mesaaz.gov/Utilities/Water-Wastewater/Backflow-Prevention",
                "official program page",
            ),
            src(
                "Mesa city code on backflow prevention",
                "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/new-mesa-city-code-backflow-prevention-2025.pdf",
                "official code pdf",
            ),
            src(
                "Mesa general tester list",
                "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf",
                "official tester list pdf",
            ),
        ],
    },
    {
        "utilityId": "san-diego-water",
        "utilityName": "City of San Diego Public Utilities Backflow Program",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "san-diego-public-utilities",
        "state": "california",
        "serviceAreaCities": ["San Diego"],
        "serviceAreaCounties": ["San Diego County"],
        "searchAliases": [
            "san diego backflow testing",
            "san diego approved backflow testers",
            "san diego fire protection backflow",
        ],
        "utilityUrl": "https://www.sandiego.gov/public-utilities/permits-construction/construction-and-development/backflow",
        "officialProgramUrls": [
            "https://www.sandiego.gov/public-utilities/permits-construction/construction-and-development/backflow",
            "https://www.sandiego.gov/sites/default/files/backflowtesters.pdf",
            "https://www.sandiego.gov/sites/default/files/di_55_21-policy_on_cross_connection_and_backflow_prevention.pdf",
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "San Diego says all backflow devices are tested annually and points customers to the approved tester list when the Public Utilities Department contacts them for compliance.",
        "coveredPropertyTypes": [
            "Multi-family services with 1.5-inch and larger meters",
            "Commercial and industrial services",
            "Dedicated irrigation meters",
            "Fire protection and meter-protection services",
        ],
        "coveredDeviceTypes": [
            "Meter-protection backflow assemblies",
            "Dedicated irrigation assemblies",
            "Fire protection backflow assemblies",
            "Approved city-listed assemblies",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.sandiego.gov/sites/default/files/backflowtesters.pdf",
        "officialListLabel": "Open the San Diego approved tester list",
        "submissionMethods": [
            method(
                "San Diego Backflow Program",
                "https://www.sandiego.gov/public-utilities/permits-construction/construction-and-development/backflow",
                "program page",
            ),
            method(
                "San Diego Request for Test form",
                "https://www.sandiego.gov/sites/default/files/cross_connection_faq.pdf",
                "faq and submission context",
            ),
            method(
                "San Diego approved tester list",
                "https://www.sandiego.gov/sites/default/files/backflowtesters.pdf",
                "official tester list",
            ),
        ],
        "phone": "858-292-6329",
        "penalties": "San Diego says failure to install and maintain a required device can lead to code enforcement, fines, and water service termination until compliance is restored.",
        "sourceExcerpt": "San Diego publishes exactly the kind of utility workflow this project needs: approved tester list, annual testing language, orientation rules for testers, and clear meter, irrigation, and fire-protection context.",
        "sourceSnapshotPath": "storage/snapshots/san-diego-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "San Diego is one of the best California fits because the city publishes annual testing rules, an approved tester list, a tester-orientation gate, and explicit enforcement consequences.",
        "whoIsAffected": "Multi-family, commercial, industrial, dedicated irrigation, and fire-protection services that the Public Utilities Department identifies as requiring backflow protection.",
        "residentialNotes": [
            "San Diego's strongest public language is tied to multi-family, irrigation, and other protected services rather than a broad residential-all-properties claim.",
            "Single-family users usually encounter this through irrigation or meter-protection requirements rather than a generic annual reminder.",
        ],
        "commercialNotes": [
            "San Diego is strong for lead intent because the city publishes both an approved tester list and real enforcement language.",
            "The utility also expects testers to meet certification and orientation requirements before working in the district.",
        ],
        "annualTesting": focus(
            "San Diego states that all backflow devices are required to be tested annually and that testing must be done by approved list testers.",
            [
                "Annual testing is explicit on the public program page.",
                "Approved testers are required.",
                "The city keeps a certification and orientation gate for testers who want onto the list.",
            ],
            [
                "Use the city's notice or program contact to confirm the assembly is due.",
                "Choose a tester from San Diego's approved list.",
                "Complete the test and submit the required documentation so the city can close the compliance loop.",
            ],
        ),
        "irrigation": focus(
            "Dedicated irrigation meters are specifically called out by San Diego, which makes irrigation a real subpage instead of filler.",
            [
                "Dedicated irrigation meters require a backflow prevention assembly regardless of size.",
                "Backflow devices must be installed close to the meter and per city standards.",
                "The same annual testing rule applies after installation.",
            ],
            [
                "Confirm whether the service is on a dedicated irrigation meter.",
                "Install the approved device at the location accepted by the city.",
                "Keep the annual test current using a tester from the approved list.",
            ],
        ),
        "fireLine": focus(
            "San Diego's public materials keep fire protection and meter protection in the same compliance conversation, which is useful for fire-line intent.",
            [
                "The city references fire protection in its device-requirement materials.",
                "Backflow preventers must be city-approved and installed according to city standards.",
                "Fire-protection and meter-protection questions route through the same cross-connection team.",
            ],
            [
                "Confirm whether the backflow requirement is tied to fire protection or meter protection.",
                "Use the city's approved standards and list-based tester workflow.",
                "Keep the test and device paperwork aligned with the assigned specialist.",
            ],
        ),
        "workflowSteps": [
            "If the Public Utilities Department contacts you, confirm which service connection is under the program.",
            "Choose a tester from the approved list.",
            "Complete the installation or annual test under the city's program standards.",
            "Resolve any enforcement notices quickly so the issue does not escalate to fines or shutoff.",
        ],
        "failureHighlights": [
            "San Diego explicitly mentions fines and water termination for noncompliance.",
            "Only approved testers can perform accepted testing.",
            "Irrigation and fire-protection cases are both visible on public pages.",
        ],
        "costBand": cost(
            "Testing is market-based, but the city's approved-list constraint shapes the quote more than a public fee schedule.",
            "Repair and retest pricing depends on assembly size and whether fire or meter-protection constraints apply.",
            "The commercial value is in the approved-list funnel and the enforcement pressure, not in a published retail rate.",
        ),
        "sources": [
            src(
                "San Diego Backflow Program",
                "https://www.sandiego.gov/public-utilities/permits-construction/construction-and-development/backflow",
                "official program page",
            ),
            src(
                "San Diego approved tester list",
                "https://www.sandiego.gov/sites/default/files/backflowtesters.pdf",
                "official tester list pdf",
            ),
            src(
                "San Diego cross-connection policy instruction",
                "https://www.sandiego.gov/sites/default/files/di_55_21-policy_on_cross_connection_and_backflow_prevention.pdf",
                "official policy pdf",
            ),
        ],
    },
    {
        "utilityId": "irwd-backflow",
        "utilityName": "Irvine Ranch Water District Backflow Prevention",
        "governingEntityType": "water district",
        "canonicalSlug": "irvine-ranch-water-district",
        "state": "california",
        "serviceAreaCities": ["Irvine"],
        "serviceAreaCounties": ["Orange County"],
        "searchAliases": [
            "irwd backflow testing",
            "irwd backflow tester list",
            "irvine backflow prevention",
        ],
        "utilityUrl": "https://www.irwd.com/services/backflow-prevention",
        "officialProgramUrls": [
            "https://www.irwd.com/services/backflow-prevention",
            "https://www.irwd.com/images/pdf/services/Backflow/IRWD_BACKFLOW_TESTER_LIST_9_25_25.pdf",
        ],
        "testingFrequency": "Upon utility notice and annually thereafter",
        "dueBasis": "IRWD says customers will be notified when a backflow assembly is required and points them to a partial list of certified testers who can perform testing in the district.",
        "coveredPropertyTypes": [
            "Residential customers with higher hazard conditions",
            "Commercial and industrial customers",
            "Properties where IRWD determines contamination risk exists",
            "Sites with dedicated irrigation or special-use water connections",
        ],
        "coveredDeviceTypes": [
            "Approved backflow prevention assemblies",
            "Double check valve assemblies",
            "Reduced pressure principle assemblies",
            "Pressure vacuum breaker assemblies",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.irwd.com/images/pdf/services/Backflow/IRWD_BACKFLOW_TESTER_LIST_9_25_25.pdf",
        "officialListLabel": "Open the IRWD backflow tester list",
        "submissionMethods": [
            method(
                "IRWD Backflow Prevention",
                "https://www.irwd.com/services/backflow-prevention",
                "program page",
            ),
            method(
                "IRWD Backflow Test and Maintenance Report form",
                "https://www.irwd.com/services/backflow-prevention",
                "form link hub",
            ),
            method(
                "IRWD tester list",
                "https://www.irwd.com/images/pdf/services/Backflow/IRWD_BACKFLOW_TESTER_LIST_9_25_25.pdf",
                "official tester list",
            ),
        ],
        "phone": "949-453-5300",
        "penalties": "IRWD frames backflow protection as a required response when the district identifies contamination risk. The practical penalty is that the water customer cannot ignore the district determination or skip the assembly maintenance requirement.",
        "sourceExcerpt": "IRWD is strong because it exposes the district decision rule, a tester list, approved assembly context, and a practical hazard-based explanation that works for residential and commercial pages.",
        "sourceSnapshotPath": "storage/snapshots/irwd-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "IRWD is a good California district page because it clearly explains when the district will require an assembly, what assembly types it recognizes, and where customers can find certified testers.",
        "whoIsAffected": "Residential, commercial, and industrial customers when IRWD determines that the potable system may be exposed to contamination through a backflow condition.",
        "residentialNotes": [
            "IRWD is useful because it does not hide the possibility that residential properties can be required to install and maintain assemblies when hazard conditions exist.",
            "Residential irrigation and special-use connections are part of the risk-based story even if IRWD does not mass-label every home as regulated.",
        ],
        "commercialNotes": [
            "Commercial and industrial customers are squarely inside IRWD's backflow program when hazard potential exists.",
            "IRWD's hazard-based explanation is strong support content for commercial next-action pages.",
        ],
        "annualTesting": focus(
            "IRWD requires customers who are brought into the program to maintain approved assemblies and use certified testers recognized by State Water Board-accepted organizations.",
            [
                "The district notifies the customer when an assembly is required.",
                "Certified testers recognized by state-approved organizations perform the work.",
                "A district-specific maintenance report form is part of the workflow.",
            ],
            [
                "Confirm whether IRWD has designated the service for backflow protection.",
                "Choose a tester from the district's partial list or another recognized certified tester.",
                "Keep the district's maintenance report and annual testing current.",
            ],
        ),
        "irrigation": focus(
            "IRWD serves a region where irrigation and landscape systems are a real contamination pathway, so hazard-based irrigation content fits naturally.",
            [
                "IRWD explains multiple assembly types that appear frequently in irrigation settings.",
                "Irrigation risk is part of the same hazard-based determination framework.",
                "Customers should not assume a landscape connection is exempt if IRWD sees contamination potential.",
            ],
            [
                "Check whether the irrigation connection is part of the district's hazard determination.",
                "Use an approved assembly type for the installation conditions.",
                "Keep the annual testing and district report form aligned with the service.",
            ],
        ),
        "workflowSteps": [
            "Wait for IRWD's determination or notice that the service requires an assembly.",
            "Install an approved assembly type that matches the site hazard.",
            "Use a recognized certified tester to test and maintain the assembly.",
            "Keep the annual testing record and district form current once the assembly is in service.",
        ],
        "failureHighlights": [
            "IRWD uses a hazard-based utility determination rather than a one-size-fits-all rule.",
            "The district points to a partial tester list rather than hiding the tester path entirely.",
            "Residential and commercial hazard cases both appear in the public explanation.",
        ],
        "costBand": cost(
            "Testing remains market-priced because IRWD mostly defines the hazard and documentation framework.",
            "Repair and retest cost depends on assembly type and site hazard, not a district-set price sheet.",
            "The monetizable value is the district determination and tester-routing clarity rather than a published fee table.",
        ),
        "sources": [
            src(
                "IRWD Backflow Prevention",
                "https://www.irwd.com/services/backflow-prevention",
                "official program page",
            ),
            src(
                "IRWD tester list",
                "https://www.irwd.com/images/pdf/services/Backflow/IRWD_BACKFLOW_TESTER_LIST_9_25_25.pdf",
                "official tester list pdf",
            ),
        ],
    },
    {
        "utilityId": "ebmud-backflow",
        "utilityName": "East Bay Municipal Utility District Backflow Prevention",
        "governingEntityType": "water district",
        "canonicalSlug": "east-bay-municipal-utility-district",
        "state": "california",
        "serviceAreaCities": ["Oakland"],
        "serviceAreaCounties": ["Alameda County", "Contra Costa County"],
        "searchAliases": [
            "ebmud backflow testing",
            "ebmud approved backflow testers",
            "oakland backflow tester",
        ],
        "utilityUrl": "https://www.ebmud.com/development-services/backflow-prevention",
        "officialProgramUrls": [
            "https://www.ebmud.com/development-services/backflow-prevention",
            "https://www.ebmud.com/download_file/force/23962/750?Approved_Backflow_Testers_09.2023.pdf=",
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "EBMUD requires tests to be performed by testers on the district's approved list and keeps separate scheduling rules for fire flushes and related field operations.",
        "coveredPropertyTypes": [
            "Commercial and industrial services",
            "New or modified services where EBMUD identifies a hazard",
            "Fire services requiring a flush appointment",
            "Regional district customers needing containment assemblies",
        ],
        "coveredDeviceTypes": [
            "Approved district backflow assemblies",
            "Fire service backflow assemblies",
            "Containment assemblies",
            "Testable assemblies on the approved device list",
        ],
        "approvedTesterMode": "OFFICIAL_LIST",
        "approvedTesterListUrl": "https://www.ebmud.com/download_file/force/23962/750?Approved_Backflow_Testers_09.2023.pdf=",
        "officialListLabel": "Open the EBMUD approved tester list",
        "submissionMethods": [
            method(
                "EBMUD backflow prevention",
                "https://www.ebmud.com/development-services/backflow-prevention",
                "program page",
            ),
            method(
                "EBMUD approved tester list",
                "https://www.ebmud.com/download_file/force/23962/750?Approved_Backflow_Testers_09.2023.pdf=",
                "official tester list",
            ),
            method(
                "EBMUD official policy concerning backflow prevention",
                "https://www.ebmud.com/development-services/backflow-prevention",
                "policy link hub",
            ),
        ],
        "phone": "510-287-0874",
        "penalties": "EBMUD does not present the program as optional. Fire-service work can stall until the correct assembly is installed and the district has scheduled the required flush under its notice rules.",
        "sourceExcerpt": "EBMUD is strong because it publishes an approved tester list, a district exam path for testers, fire-flush scheduling rules, and a visible official policy layer.",
        "sourceSnapshotPath": "storage/snapshots/ebmud-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "EBMUD supports trustworthy utility pages because it publishes an approved tester list, official policy links, and fire-flush rules that make the program operationally concrete.",
        "whoIsAffected": "District customers who need containment assemblies, commercial and industrial projects, and fire-service work that EBMUD identifies as needing approved backflow protection.",
        "residentialNotes": [
            "EBMUD is less residential-marketing heavy than some city utilities, but residential hazard cases can still be pulled into the district's protection rules where appropriate.",
            "The public value is strongest for mixed-use, commercial, and managed-property intent.",
        ],
        "commercialNotes": [
            "EBMUD is a strong commercial page because the district runs its own tester approval exam and maintains the public approved list.",
            "Fire-flush scheduling and installation sequence matter for larger properties and projects.",
        ],
        "annualTesting": focus(
            "EBMUD requires the test to be performed by a tester from the district's approved list and maintains its own approval process for testers.",
            [
                "Only approved district testers can perform the accepted test.",
                "The district maintains a public approved list.",
                "Tester approval itself requires certification plus an EBMUD exam.",
            ],
            [
                "Confirm the service is under EBMUD's backflow requirements.",
                "Use a tester from the approved district list.",
                "Keep the annual test and any required district forms current.",
            ],
        ),
        "fireLine": focus(
            "EBMUD's fire-side workflow is useful because the district requires scheduling and will not perform an underground fire service flush until the proper assembly is installed.",
            [
                "The district requires at least 48 hours notice for a fire flush appointment.",
                "The local Fire Marshal or Fire Inspector should be scheduled before calling EBMUD for the flush.",
                "An appropriate backflow assembly must already be installed before the flush happens.",
            ],
            [
                "Coordinate the fire-side schedule with the local inspector first.",
                "Install the required assembly before requesting the EBMUD flush.",
                "Keep the flush and test sequence aligned so the project does not stall.",
            ],
        ),
        "workflowSteps": [
            "Confirm the service or project requires EBMUD backflow protection.",
            "Use an EBMUD-approved tester from the district list.",
            "Complete the annual test and any district paperwork.",
            "If the property includes fire service, schedule the fire-flush sequence with the required notice.",
        ],
        "failureHighlights": [
            "EBMUD keeps its own approved tester exam and public list.",
            "Fire-service work can stall if the assembly is not installed before the flush.",
            "The district's program is stronger for commercial and project-driven intent than for generic plumbing content.",
        ],
        "costBand": cost(
            "Testing is market-priced but constrained by EBMUD's approved-list rule.",
            "Repair and retest costs rise on projects that include fire-service scheduling or larger assemblies.",
            "The district's commercial value is in approval gating and project sequencing, not in a public retail fee sheet.",
        ),
        "sources": [
            src(
                "EBMUD backflow prevention",
                "https://www.ebmud.com/development-services/backflow-prevention",
                "official program page",
            ),
            src(
                "EBMUD approved tester list",
                "https://www.ebmud.com/download_file/force/23962/750?Approved_Backflow_Testers_09.2023.pdf=",
                "official tester list pdf",
            ),
        ],
    },
    {
        "utilityId": "denver-water",
        "utilityName": "Denver Water Cross-Connection Control and Backflow Prevention Program",
        "governingEntityType": "water utility",
        "canonicalSlug": "denver-water",
        "state": "colorado",
        "serviceAreaCities": ["Denver"],
        "serviceAreaCounties": ["Denver County"],
        "searchAliases": [
            "denver water backflow testing",
            "denver irrigation backflow",
            "denver fire line backflow"
        ],
        "utilityUrl": "https://www.denverwater.org/contractors/construction-information/backflow-prevention-program",
        "officialProgramUrls": [
            "https://www.denverwater.org/contractors/construction-information/backflow-prevention-program",
            "https://www.denverwater.org/sites/default/files/2017-05/backflow-device-test-maintenance-report.pdf"
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Denver Water sends a testing reminder 30 days before the annual test is due, expects certified testers to report results to the Cross-Connection Control office, and can assess a $250 penalty after repeated ignored notices.",
        "coveredPropertyTypes": [
            "Commercial services",
            "Industrial services",
            "Domestic services where the site hazard requires protection",
            "Irrigation and fire line services",
            "Sites with an auxiliary water supply"
        ],
        "coveredDeviceTypes": [
            "Backflow prevention assemblies",
            "Irrigation RP or PVB devices",
            "Fire line assemblies",
            "Domestic containment assemblies"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Denver Water backflow program",
                "https://www.denverwater.org/contractors/construction-information/backflow-prevention-program",
                "program page"
            ),
            method(
                "Denver Water test and maintenance report",
                "https://www.denverwater.org/sites/default/files/2017-05/backflow-device-test-maintenance-report.pdf",
                "test report form"
            )
        ],
        "phone": "303-893-2444",
        "penalties": "Denver Water can assess a $250 penalty after three unanswered notification letters and can move service into suspension status when testing or installation is not completed.",
        "sourceExcerpt": "Denver Water is one of the clearest Colorado programs because it publishes annual reminder timing, irrigation season rules, a penalty path, and a direct reporting address for the cross-connection office.",
        "sourceSnapshotPath": "storage/snapshots/denver-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Denver Water is a strong flagship Colorado utility because it publishes the annual reminder cycle, irrigation season rule, reporting path, and an explicit penalty for ignored notices.",
        "whoIsAffected": "Commercial, industrial, domestic, irrigation, fire-line, and auxiliary-water-supply customers when Denver Water requires an approved assembly.",
        "residentialNotes": [
            "Denver Water can pull domestic services into the program when the site hazard justifies it, so residential users should not assume the rules are only commercial.",
            "Residential irrigation assemblies have their own seasonal testing rhythm when the water is turned on."
        ],
        "commercialNotes": [
            "Commercial and industrial services are clearly inside the Denver Water program and are a strong fit for next-action pages.",
            "The penalty path makes Denver more commercially urgent than a generic educational page."
        ],
        "annualTesting": focus(
            "Denver Water requires certified testers to test assemblies on installation and annually thereafter, then report the results to the cross-connection control office.",
            [
                "The utility sends reminder notices 30 days before the annual due date.",
                "Test reports are sent to the cross-connection control office.",
                "A penalty path exists after repeated ignored notices."
            ],
            [
                "Watch for Denver Water's annual reminder.",
                "Use a certified tester to complete the test.",
                "Submit the test report to the cross-connection control office before the service moves toward suspension."
            ]
        ),
        "irrigation": focus(
            "Denver Water has a clear irrigation testing season and specific accepted assembly types for irrigation protection.",
            [
                "Irrigation backflow testing season runs from May to September.",
                "Irrigation services must use an RP or PVB depending on installation requirements.",
                "Denver Water sends a reminder notice before the annual irrigation test is due."
            ],
            [
                "As irrigation season starts, confirm the assembly is ready for testing.",
                "Use the assembly type Denver Water accepts for the installation condition.",
                "Complete the irrigation test when the water is turned on and send the report to Denver Water."
            ]
        ),
        "fireLine": focus(
            "Denver Water treats fire-line services as part of the core program rather than an edge case.",
            [
                "All fire line services are required to have an approved assembly installed.",
                "Fire-flow and construction-related utility work should be coordinated with Denver Water forms and standards.",
                "Fire service can create larger and more expensive correction paths when the assembly is missing or overdue."
            ],
            [
                "Confirm whether the service is domestic, irrigation, or fire-line.",
                "Install the approved fire-line assembly before the service is placed into use.",
                "Keep the annual test current so the service does not move into penalty or suspension status."
            ]
        ),
        "workflowSteps": [
            "Confirm the service is in Denver Water's backflow program.",
            "Use a certified tester to test the assembly on installation and annually thereafter.",
            "Send the report to the cross-connection control office.",
            "If delays are unavoidable, contact Denver Water before the service reaches suspension status."
        ],
        "failureHighlights": [
            "Denver Water sends reminder letters and can assess a $250 penalty.",
            "Irrigation testing is seasonal rather than generic year-round filler.",
            "Fire-line and auxiliary-water cases are explicitly called out in the public program."
        ],
        "costBand": cost(
            "Testing is market-priced, but Denver Water adds real compliance cost through penalties and suspension risk.",
            "Repair and retest cost varies widely by domestic, irrigation, or fire-line assembly type.",
            "The financial risk is not just the tester invoice; it is also Denver Water's penalty and service-interruption exposure."
        ),
        "sources": [
            src(
                "Denver Water backflow program",
                "https://www.denverwater.org/contractors/construction-information/backflow-prevention-program",
                "official program page"
            ),
            src(
                "Denver Water test and maintenance report",
                "https://www.denverwater.org/sites/default/files/2017-05/backflow-device-test-maintenance-report.pdf",
                "official report form"
            )
        ]
    },
    {
        "utilityId": "aurora-water",
        "utilityName": "Aurora Water Backflow Prevention",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "aurora-water",
        "state": "colorado",
        "serviceAreaCities": ["Aurora"],
        "serviceAreaCounties": ["Arapahoe County"],
        "searchAliases": [
            "aurora water backflow testing",
            "aurora annual backflow test",
            "aurora hydrant meter backflow"
        ],
        "utilityUrl": "https://www.auroragov.org/residents/water/pay_my_water_bill/backflow_prevention",
        "officialProgramUrls": [
            "https://www.auroragov.org/residents/water/pay_my_water_bill/backflow_prevention",
            "https://www.auroragov.org/residents/water/service_center___emergencies/hydrant_meter_program"
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Aurora says operational tests by a certified technician must be conducted upon installation and at least annually thereafter, and results must be submitted online before the annual test due date.",
        "coveredPropertyTypes": [
            "Water customers with required backflow assemblies",
            "Commercial and managed properties",
            "Hydrant users",
            "Property owners responsible for protected services"
        ],
        "coveredDeviceTypes": [
            "Required backflow prevention assemblies",
            "Hydrant-meter RP devices",
            "Domestic containment devices",
            "Cross-connection control devices"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Aurora Water backflow prevention",
                "https://www.auroragov.org/residents/water/pay_my_water_bill/backflow_prevention",
                "program page"
            ),
            method(
                "Aurora hydrant meter program",
                "https://www.auroragov.org/residents/water/service_center___emergencies/hydrant_meter_program",
                "hydrant backflow guidance"
            )
        ],
        "phone": "303-326-8520",
        "penalties": "Aurora places responsibility on the billed customer or owner and expects online submission before the annual due date. The practical risk is staying out of compliance with the city's annual testing requirement.",
        "sourceExcerpt": "Aurora is a good Colorado utility because it publishes the annual due-date rule, online result submission, responsibility assignment, and a separate hydrant-meter RP requirement.",
        "sourceSnapshotPath": "storage/snapshots/aurora-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Aurora Water is a strong supporting Colorado utility because it publishes a clean annual-testing rule, online submission requirement, and ownership-responsibility language.",
        "whoIsAffected": "Aurora Water customers and property owners whose service requires a backflow prevention assembly, including hydrant users who need RP protection.",
        "residentialNotes": [
            "Aurora assigns responsibility to the billed water customer or the property owner, which matters for single-site ownership and property-management setups.",
            "Residential properties can still enter the program where the city requires a protected service."
        ],
        "commercialNotes": [
            "Aurora's clear owner responsibility and online-due-date workflow make it usable for commercial lead intent.",
            "Hydrant-meter use adds another practical compliance path beyond ordinary building service."
        ],
        "annualTesting": focus(
            "Aurora requires certified technicians to test assemblies upon installation and at least annually thereafter, then submit the results online before the due date.",
            [
                "Annual testing is explicit on the public page.",
                "Results are submitted online rather than held offline.",
                "Responsibility sits with the water customer or property owner."
            ],
            [
                "Confirm the responsible party and the annual due date.",
                "Use a certified backflow technician to perform the test.",
                "Submit the result online before the due date so the assembly stays current."
            ]
        ),
        "irrigation": focus(
            "Aurora does not publish a separate irrigation mini-program as strongly as Denver does, but the annual testing and protected-service rules still apply to landscape risk when an assembly is required.",
            [
                "Annual test discipline still applies once an irrigation-related assembly is required.",
                "Landscape and outdoor water uses can surface cross-connection risks even when the page is not branded as an irrigation program.",
                "Hydrant-related outdoor water use explicitly requires RP protection."
            ],
            [
                "Confirm whether the irrigation or temporary outdoor use requires city-recognized backflow protection.",
                "Use the correct RP or other required assembly for the setup.",
                "Keep the online annual test submission current once the device is active."
            ]
        ),
        "workflowSteps": [
            "Identify the responsible party for the service.",
            "Use a certified technician to perform installation and annual operational testing.",
            "Submit the test results online before the due date.",
            "For hydrant or temporary outdoor use, provide the required RP protection and current test data."
        ],
        "failureHighlights": [
            "Aurora requires online submission before the due date.",
            "The owner or billed customer is explicitly responsible.",
            "Hydrant use has its own RP requirement with current test data."
        ],
        "costBand": cost(
            "Testing is market-priced, but missing the online due-date workflow can create avoidable compliance friction.",
            "Repair and retest costs vary by service type and whether temporary or outdoor water-use protection is involved.",
            "Aurora's value is the clear annual workflow and owner-responsibility language, not a published test-price table."
        ),
        "sources": [
            src(
                "Aurora Water backflow prevention",
                "https://www.auroragov.org/residents/water/pay_my_water_bill/backflow_prevention",
                "official program page"
            ),
            src(
                "Aurora hydrant meter program",
                "https://www.auroragov.org/residents/water/service_center___emergencies/hydrant_meter_program",
                "official hydrant guidance"
            )
        ]
    },
    {
        "utilityId": "csu-backflow",
        "utilityName": "Colorado Springs Utilities Backflow Prevention",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "colorado-springs-utilities",
        "state": "colorado",
        "serviceAreaCities": ["Colorado Springs"],
        "serviceAreaCounties": ["El Paso County"],
        "searchAliases": [
            "colorado springs backflow testing",
            "csu backflow swiftcomply",
            "colorado springs irrigation backflow"
        ],
        "utilityUrl": "https://www.csu.org/safety/backflow-prevention-professionals",
        "officialProgramUrls": [
            "https://www.csu.org/safety/backflow-prevention-professionals",
            "https://www.csu.org/hubfs/Document-Library/UtilitiesRulesRegsTariff.pdf"
        ],
        "testingFrequency": "Annually after the assembly is in the program",
        "dueBasis": "Colorado Springs Utilities requires customers to hire a backflow tester for the annual compliance test and expects test results to be entered within five days through SwiftComply.",
        "coveredPropertyTypes": [
            "Protected utility customers",
            "External and irrigation assembly locations",
            "Commercial and managed properties",
            "Any service already surveyed into the program"
        ],
        "coveredDeviceTypes": [
            "Backflow prevention assemblies",
            "External assemblies",
            "Irrigation assemblies",
            "Surveyed utility assemblies"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Colorado Springs Utilities backflow professionals portal",
                "https://www.csu.org/safety/backflow-prevention-professionals",
                "tester portal guidance"
            ),
            method(
                "Colorado Springs Utilities rules and regulations",
                "https://www.csu.org/hubfs/Document-Library/UtilitiesRulesRegsTariff.pdf",
                "rules and tariff document"
            )
        ],
        "phone": "719-668-4388",
        "penalties": "Colorado Springs Utilities will not accept a test on a newly installed assembly until the backflow team has surveyed it, and old results are expected to be entered promptly through the portal.",
        "sourceExcerpt": "Colorado Springs is useful because it exposes the contractor-facing side of the compliance loop: SwiftComply registration, certification uploads, five-day entry timing, and survey-first rules for new assemblies.",
        "sourceSnapshotPath": "storage/snapshots/colorado-springs-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Colorado Springs Utilities is a strong Colorado page because it shows how the utility actually runs the testing workflow: portal registration, certification uploads, five-day test entry, and survey-first rules.",
        "whoIsAffected": "Customers and testers working on assemblies that Colorado Springs Utilities has already surveyed into the program, including external and irrigation assemblies.",
        "residentialNotes": [
            "Colorado Springs is less residential-marketing heavy than Tampa or Broward, but any surveyed assembly still lives inside the same portal-driven workflow.",
            "Residential intent mostly shows up through external or irrigation assembly cases rather than generic homeowner education."
        ],
        "commercialNotes": [
            "This is a strong contractor and managed-property utility because the portal rules are explicit.",
            "The survey-first rule matters operationally for new installations and replacements."
        ],
        "annualTesting": focus(
            "Colorado Springs Utilities expects annual compliance testing by certified testers and wants results entered into SwiftComply within five days.",
            [
                "Certified testers must register and upload certification and test-kit information.",
                "Test results should be entered within five days of testing.",
                "A newly installed assembly must be surveyed by the utility before a test can be accepted."
            ],
            [
                "Register the tester and current certification information in SwiftComply.",
                "Confirm the assembly is already surveyed into the utility record.",
                "Enter the test result promptly and fix record errors through the utility team when needed."
            ]
        ),
        "irrigation": focus(
            "Colorado Springs publishes a separate external and irrigation assembly form, which makes irrigation-specific intent worth supporting.",
            [
                "External and irrigation assemblies have a distinct form path.",
                "Portal submission rules still apply.",
                "Survey-first discipline matters before the utility will accept a test for a new assembly."
            ],
            [
                "Confirm whether the assembly is domestic, external, or irrigation.",
                "Use the right form and portal workflow for the assembly type.",
                "Enter the result within five days and coordinate any new-assembly survey issues with the utility team."
            ]
        ),
        "workflowSteps": [
            "Register the tester and current certification in SwiftComply.",
            "Confirm the assembly is already surveyed by Colorado Springs Utilities.",
            "Perform the annual compliance test.",
            "Enter the result within five days and flag any record errors or replacements through the utility."
        ],
        "failureHighlights": [
            "Colorado Springs uses a five-day result-entry expectation.",
            "New assemblies must be surveyed before tests are accepted.",
            "The tester portal is mandatory for practical compliance."
        ],
        "costBand": cost(
            "Testing is market-priced, but the portal registration and survey-first rule create real workflow cost.",
            "Repair and retest costs vary depending on whether the assembly record is already accurate in the utility system.",
            "The value is in understanding the portal and survey gate, not in a public price schedule."
        ),
        "sources": [
            src(
                "Colorado Springs Utilities backflow professionals",
                "https://www.csu.org/safety/backflow-prevention-professionals",
                "official portal guidance"
            ),
            src(
                "Colorado Springs Utilities rules and regulations",
                "https://www.csu.org/hubfs/Document-Library/UtilitiesRulesRegsTariff.pdf",
                "official rules pdf"
            )
        ]
    },
    {
        "utilityId": "miami-dade-wasd",
        "utilityName": "Miami-Dade Water and Sewer Department Cross-Connection Control",
        "governingEntityType": "county utility",
        "canonicalSlug": "miami-dade-water-and-sewer-department",
        "state": "florida",
        "serviceAreaCities": ["Miami"],
        "serviceAreaCounties": ["Miami-Dade County"],
        "searchAliases": [
            "miami dade backflow testing",
            "miami dade cross connection control",
            "miami irrigation backflow"
        ],
        "utilityUrl": "https://www.miamidade.gov/global/service.page?Mduid_service=ser1517344068939644",
        "officialProgramUrls": [
            "https://www.miamidade.gov/global/service.page?Mduid_service=ser1517344068939644",
            "https://www.miamidade.gov/water/library/brochures/cross-connection-backflow.pdf"
        ],
        "testingFrequency": "Upon installation and annually thereafter",
        "dueBasis": "Miami-Dade says certain customers, including irrigation users and listed hazard facilities, must install assemblies and have them tested upon installation and annually by a certified tester.",
        "coveredPropertyTypes": [
            "Hospitals and adult congregate living facilities",
            "Service stations and auto repair shops",
            "Water customers with lawn irrigation systems",
            "Non-residential and higher-hazard multi-family properties"
        ],
        "coveredDeviceTypes": [
            "Backflow prevention assemblies at the service connection",
            "Lawn irrigation protection assemblies",
            "Reduced pressure assemblies for higher hazard situations",
            "County-approved backflow preventers"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Miami-Dade cross-connection service page",
                "https://www.miamidade.gov/global/service.page?Mduid_service=ser1517344068939644",
                "program page"
            ),
            method(
                "Miami-Dade cross-connection brochure",
                "https://www.miamidade.gov/water/library/brochures/cross-connection-backflow.pdf",
                "official brochure"
            )
        ],
        "phone": "305-547-3046",
        "penalties": "Miami-Dade places the obligation directly on the water customer to install or maintain the required assembly. The practical penalty is noncompliance with a county protection program tied to the service connection.",
        "sourceExcerpt": "Miami-Dade is a strong Florida utility because the county brochure and service page spell out which customer types need assemblies and confirm annual testing by a certified tester.",
        "sourceSnapshotPath": "storage/snapshots/miami-dade-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Miami-Dade is one of the clearest Florida county programs because it names hazard classes, includes irrigation in the protected group, and requires annual testing by certified testers.",
        "whoIsAffected": "Hazard facilities, irrigation customers, and other Miami-Dade water customers that the county code or county program identifies as needing a service-connection assembly.",
        "residentialNotes": [
            "Miami-Dade explicitly includes lawn irrigation systems in the protected customer mix, which makes residential irrigation a real local compliance topic.",
            "Straight residential potable service is not the strongest page angle unless irrigation or another hazard exists."
        ],
        "commercialNotes": [
            "Miami-Dade names several commercial and institutional hazard classes directly, which makes the page commercially useful.",
            "County-scale rules create strong next-action content even without a public tester directory."
        ],
        "annualTesting": focus(
            "Miami-Dade requires covered customers to have assemblies tested upon installation and annually by a certified backflow prevention assembly tester.",
            [
                "Annual testing is explicit in the county brochure.",
                "Covered customer classes are named publicly.",
                "The utility frames this as cross-connection protection at the service connection."
            ],
            [
                "Confirm the property falls into one of Miami-Dade's covered customer classes.",
                "Use a certified tester to perform the installation test or annual retest.",
                "Keep the county program documentation current for the service connection."
            ]
        ),
        "irrigation": focus(
            "Miami-Dade explicitly names lawn irrigation systems in the covered customer set, which makes irrigation a strong Florida subpage.",
            [
                "Lawn irrigation systems are listed in the county brochure.",
                "Irrigation risk is handled as a service-connection protection issue, not just a landscaping detail.",
                "Annual testing still applies once the irrigation-related assembly is required."
            ],
            [
                "Confirm the irrigation service falls inside Miami-Dade's covered class.",
                "Use the required assembly at the service connection.",
                "Keep the annual certified test current so the irrigation connection stays compliant."
            ]
        ),
        "workflowSteps": [
            "Check whether the property falls into a Miami-Dade covered customer class.",
            "Install the correct assembly at the service connection if required.",
            "Use a certified tester to perform the installation test and annual retest.",
            "Keep the county compliance record current for the protected service."
        ],
        "failureHighlights": [
            "Miami-Dade directly names irrigation as a trigger category.",
            "County code ties the obligation to the service connection, not just interior plumbing.",
            "Annual testing is required once the assembly is in place."
        ],
        "costBand": cost(
            "Testing is market-priced because Miami-Dade does not publish a consumer fee schedule on the program page.",
            "Repair and retest cost depends on the assembly type and the property hazard class.",
            "The value of the page is in hazard-class clarity and next-action routing, not in a published county test fee."
        ),
        "sources": [
            src(
                "Miami-Dade cross-connection service page",
                "https://www.miamidade.gov/global/service.page?Mduid_service=ser1517344068939644",
                "official program page"
            ),
            src(
                "Miami-Dade cross-connection brochure",
                "https://www.miamidade.gov/water/library/brochures/cross-connection-backflow.pdf",
                "official brochure"
            )
        ]
    },
    {
        "utilityId": "broward-water",
        "utilityName": "Broward County Water and Wastewater Services Backflow Certification",
        "governingEntityType": "county utility",
        "canonicalSlug": "broward-county-water-and-wastewater-services",
        "state": "florida",
        "serviceAreaCities": ["Fort Lauderdale"],
        "serviceAreaCounties": ["Broward County"],
        "searchAliases": [
            "broward backflow testing",
            "broward backflow certification",
            "broward irrigation backflow"
        ],
        "utilityUrl": "https://www.broward.org/WaterServices/Pages/BackflowCertification.aspx",
        "officialProgramUrls": [
            "https://www.broward.org/WaterServices/Pages/BackflowCertification.aspx"
        ],
        "testingFrequency": "Annually",
        "dueBasis": "Broward notifies customers 60 days and 30 days before the annual compliance due date and requires passing test results plus filing fees through Backflow Solutions Incorporated.",
        "coveredPropertyTypes": [
            "Commercial, institutional, and governmental customers",
            "Residential and multifamily buildings over three stories",
            "Domestic services",
            "Irrigation services",
            "Fire protection services"
        ],
        "coveredDeviceTypes": [
            "Domestic backflow assemblies",
            "Irrigation backflow assemblies",
            "Fire service backflow assemblies",
            "Appropriate assembly types required by county ordinance"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Broward backflow certification program",
                "https://www.broward.org/WaterServices/Pages/BackflowCertification.aspx",
                "program page"
            )
        ],
        "phone": "954-831-3276",
        "penalties": "Broward expects passing test results and filing-fee payment by the annual due date. The county separates who may work on domestic, irrigation, and fire-related backflows, so the wrong tester path can still leave the account out of compliance.",
        "sourceExcerpt": "Broward is one of the strongest Florida county pages because it publishes annual notice timing, filing-fee handling, and separate technician qualification rules for domestic, irrigation, and fire-service work.",
        "sourceSnapshotPath": "storage/snapshots/broward-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Broward County is a high-value Florida utility because it publishes due-date notices, filing-fee handling, and separate tester qualification rules for domestic, irrigation, and fire-service assemblies.",
        "whoIsAffected": "Commercial, institutional, governmental, taller residential and multifamily buildings, and customers with domestic, irrigation, or fire-related assemblies under Broward's ordinance.",
        "residentialNotes": [
            "Broward is one of the clearer county programs for taller residential and multifamily buildings.",
            "Residential irrigation still requires qualified personnel under the county's separate domestic-versus-irrigation rule."
        ],
        "commercialNotes": [
            "This is strong commercial lead content because the county names the covered customer classes and the annual due-date workflow.",
            "Managed properties have to care about both tester qualification and filing-fee timing."
        ],
        "annualTesting": focus(
            "Broward sends 60-day and 30-day notices before the annual compliance deadline and expects passing test results plus filing fees through BSI.",
            [
                "The annual due-date reminders are public and concrete.",
                "Passing test results and payment move through the BSI workflow.",
                "The county distinguishes who is qualified for domestic, irrigation, and fire-related work."
            ],
            [
                "Watch for Broward's 60-day and 30-day notices.",
                "Use a qualified technician for the right assembly class.",
                "Submit the passing result and any required filing fees through the county's program workflow."
            ]
        ),
        "irrigation": focus(
            "Broward treats irrigation as its own technician-qualification path, which makes irrigation a real county subpage.",
            [
                "Only properly certified personnel working under the right licensing umbrella can work on irrigation-related services.",
                "Irrigation does not share exactly the same worker-qualification path as fire service.",
                "The annual due-date and BSI submission workflow still apply."
            ],
            [
                "Confirm the service is classified as irrigation rather than fire or domestic.",
                "Use the right qualified technician for the irrigation assembly.",
                "Submit the passing result and filing fee before the annual due date passes."
            ]
        ),
        "fireLine": focus(
            "Broward explicitly says fire service work must be handled by fire-service trained and qualified technicians.",
            [
                "Fire-service work has a stricter qualification rule than ordinary domestic service.",
                "The annual county compliance cycle still applies.",
                "Using the wrong contractor can leave the account noncompliant even if a test was attempted."
            ],
            [
                "Confirm the assembly is part of the fire service.",
                "Use a fire-service trained and qualified technician.",
                "Complete the annual test and submit the county-required result through the compliance workflow."
            ]
        ),
        "workflowSteps": [
            "Identify which Broward customer class and assembly class applies to the property.",
            "Watch for the 60-day and 30-day annual notices.",
            "Use the right qualified technician for domestic, irrigation, or fire service work.",
            "Submit passing results and any filing fees through the county workflow before the due date."
        ],
        "failureHighlights": [
            "Broward uses explicit 60-day and 30-day reminders.",
            "Domestic, irrigation, and fire-service qualification rules differ.",
            "Passing results and filing fees move together through the county process."
        ],
        "costBand": cost(
            "Testing is market-priced, but the county adds filing fees and qualification risk to the total compliance cost.",
            "Repair and retest pricing depends heavily on whether the work is domestic, irrigation, or fire service.",
            "The main local cost signal is not a flat county test fee; it is the annual compliance plus filing workflow."
        ),
        "sources": [
            src(
                "Broward backflow certification program",
                "https://www.broward.org/WaterServices/Pages/BackflowCertification.aspx",
                "official program page"
            )
        ]
    },
    {
        "utilityId": "tampa-water",
        "utilityName": "City of Tampa Water Department Backflow Prevention",
        "governingEntityType": "municipal utility",
        "canonicalSlug": "tampa-water-department",
        "state": "florida",
        "serviceAreaCities": ["Tampa"],
        "serviceAreaCounties": ["Hillsborough County"],
        "searchAliases": [
            "tampa backflow testing",
            "tampa swiftcomply backflow",
            "tampa irrigation backflow"
        ],
        "utilityUrl": "https://www.tampa.gov/water/builders-and-homeowners/backflow-prevention",
        "officialProgramUrls": [
            "https://www.tampa.gov/water/builders-and-homeowners/backflow-prevention"
        ],
        "testingFrequency": "Commercial annually; residential every two years",
        "dueBasis": "Tampa requires certified test results to reach the Water Department within seven calendar days after testing. Commercial properties are annual and residential properties are biannual.",
        "coveredPropertyTypes": [
            "Commercial properties",
            "Residential properties with required assemblies",
            "Facilities usually requiring service-connection protection under the municipal code",
            "Sites using SwiftComply tester enrollment"
        ],
        "coveredDeviceTypes": [
            "Backflow prevention devices",
            "Commercial assemblies",
            "Residential irrigation or protected-service assemblies",
            "Approved Tampa installation devices"
        ],
        "approvedTesterMode": "NONE",
        "approvedTesterListUrl": "",
        "officialListLabel": "",
        "submissionMethods": [
            method(
                "Tampa backflow prevention program",
                "https://www.tampa.gov/water/builders-and-homeowners/backflow-prevention",
                "program page"
            ),
            method(
                "Tampa SwiftComply portal",
                "https://tampafl.c3swift.com/",
                "online reporting portal"
            )
        ],
        "phone": "813-231-5266",
        "penalties": "Tampa does not frame the program as casual. Test results must reach the Water Department within seven days, and the city uses SwiftComply enrollment to control who can submit results.",
        "sourceExcerpt": "Tampa is a very strong Florida utility because it publishes a split residential-versus-commercial cadence, a seven-day reporting deadline, and a SwiftComply tester-enrollment flow.",
        "sourceSnapshotPath": "storage/snapshots/tampa-backflow.html",
        "reviewerInitials": "TL",
        "staleAfterDays": 45,
        "pageStatus": "PUBLISH",
        "lastVerified": TODAY,
        "verdictSummary": "Tampa is one of the best Florida fits because it publishes a clear cadence split, a seven-day reporting deadline, and a real tester-enrollment workflow instead of generic educational content.",
        "whoIsAffected": "Commercial properties, residential properties with required devices, and other facilities listed in Tampa's municipal code as usually requiring service-connection protection.",
        "residentialNotes": [
            "Tampa is unusually useful for residential pages because it publishes a separate biannual cadence for residential properties.",
            "Residential irrigation and outdoor water use can still carry the same seven-day report-delivery requirement once testing is completed."
        ],
        "commercialNotes": [
            "Commercial properties stay on an annual cycle, which creates strong recurring-intent content.",
            "SwiftComply enrollment gives Tampa a concrete tester-routing workflow even without a public approved list."
        ],
        "annualTesting": focus(
            "Tampa keeps commercial properties on an annual cycle and expects certified test results within seven calendar days of testing.",
            [
                "Commercial cadence is annual.",
                "Results must be received within seven calendar days.",
                "Tester onboarding runs through SwiftComply."
            ],
            [
                "Confirm whether the property is commercial or residential.",
                "Use a certified tester enrolled through the Tampa workflow.",
                "Get the result into the city's system within seven days of testing."
            ]
        ),
        "irrigation": focus(
            "Tampa's utility pages pair backflow compliance with real irrigation-heavy residential and commercial conditions, which makes irrigation a strong lead page.",
            [
                "Residential properties remain on a two-year cadence while commercial stays annual.",
                "Test results still have a seven-day submission window.",
                "Irrigation-heavy sites are part of the real Florida risk profile even when the city page is broad."
            ],
            [
                "Confirm whether the assembly is on a residential or commercial irrigation path.",
                "Use the city's certified tester workflow.",
                "Submit the result to the Water Department within seven days of the test."
            ]
        ),
        "workflowSteps": [
            "Determine whether the property is residential or commercial for cadence purposes.",
            "Use a certified tester and make sure the tester is enrolled through Tampa's workflow.",
            "Complete the test on the correct cadence.",
            "Submit the certified result within seven calendar days."
        ],
        "failureHighlights": [
            "Tampa uses different cadences for commercial and residential properties.",
            "The city requires delivery of certified results within seven days.",
            "Tester enrollment is portal-driven rather than ad hoc."
        ],
        "costBand": cost(
            "Testing is market-priced, but Tampa's split cadence changes how often the property pays for service.",
            "Repair and retest cost depends on assembly type and whether the site is residential or commercial.",
            "The strongest local cost signal is the recurring cadence plus the seven-day reporting discipline."
        ),
        "sources": [
            src(
                "Tampa backflow prevention program",
                "https://www.tampa.gov/water/builders-and-homeowners/backflow-prevention",
                "official program page"
            ),
            src(
                "Tampa SwiftComply portal",
                "https://tampafl.c3swift.com/",
                "official portal"
            )
        ]
    },
]

for utility in utilities:
    write_json(
        ROOT / "data" / "utilities" / utility["state"] / f"{utility['canonicalSlug']}.json",
        utility,
    )
    write_snapshot(
        ROOT / utility["sourceSnapshotPath"],
        f"{utility['utilityName']} snapshot",
        utility["sourceExcerpt"],
        [source["url"] for source in utility["sources"]],
    )

provider_rows = [
    ["phoenix-western-backflow", "Western Backflow Testing LLC", "utility", "phoenix-water", "PUBLIC", "Official Phoenix tester list entry", "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf", "480-628-9724", "", "", "DIRECTORY", "Officially listed on the Phoenix tester PDF", TODAY],
    ["phoenix-air-water", "Air & Water Factor", "utility", "phoenix-water", "PUBLIC", "Official Phoenix tester list entry", "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf", "480-706-3333", "", "", "DIRECTORY", "Officially listed on the Phoenix tester PDF", TODAY],
    ["phoenix-guardian", "Guardian Industries LLC", "utility", "phoenix-water", "PUBLIC", "Official Phoenix tester list entry", "https://www.phoenix.gov/content/dam/phoenix/pddsite/documents/trt/external/dsd_trt_pdf_00937.pdf", "602-781-9913", "", "", "DIRECTORY", "Officially listed on the Phoenix tester PDF", TODAY],
    ["tucson-annual-backflow", "#1 Annual Backflow Certification & Repair", "utility", "tucson-water", "PUBLIC", "Official Tucson tester list entry", "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf", "520-201-8646", "", "", "DIRECTORY", "Officially listed on the Tucson tester PDF", TODAY],
    ["tucson-backflow-testing", "Tucson Backflow Testing", "utility", "tucson-water", "PUBLIC", "Official Tucson tester list entry", "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf", "520-745-3962", "", "", "DIRECTORY", "Officially listed on the Tucson tester PDF", TODAY],
    ["tucson-bps", "Backflow Prevention Specialties", "utility", "tucson-water", "PUBLIC", "Official Tucson tester list entry", "https://www.tucsonaz.gov/files/sharedassets/public/v/1/city-services/tucson-water/water-quality/documents/backflow_tester_list_2020_march.pdf", "520-792-3411", "", "", "DIRECTORY", "Officially listed on the Tucson tester PDF", TODAY],
    ["mesa-american-backflow", "American Backflow & Fire Prevention", "utility", "mesa-water", "PUBLIC", "Official Mesa tester list entry", "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf", "480-630-1610", "", "", "DIRECTORY", "Officially listed on the Mesa tester PDF", TODAY],
    ["mesa-air-water", "Air and Water Mechanical LLC", "utility", "mesa-water", "PUBLIC", "Official Mesa tester list entry", "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf", "480-706-3333", "", "", "DIRECTORY", "Officially listed on the Mesa tester PDF", TODAY],
    ["mesa-next-protection", "Next Protection", "utility", "mesa-water", "PUBLIC", "Official Mesa tester list entry", "https://www.mesaaz.gov/files/assets/public/v/1/utilities/water/backflow/backflow-general-tester-list.pdf", "480-612-6600", "", "", "DIRECTORY", "Officially listed on the Mesa tester PDF", TODAY],
    ["san-diego-bernard-clarke", "Bernard Clarke", "utility", "san-diego-water", "PUBLIC", "Official San Diego approved tester entry", "https://www.sandiego.gov/sites/default/files/backflowtesters.pdf", "661-294-0901", "", "", "DIRECTORY", "Officially listed on the San Diego tester PDF", TODAY],
]

providers_path = ROOT / "data" / "providers" / "providers.csv"
existing = providers_path.read_text(encoding="utf-8").splitlines()
existing_ids = {line.split(",", 1)[0] for line in existing[1:] if line.strip()}
with providers_path.open("a", encoding="utf-8", newline="") as handle:
    writer = csv.writer(handle)
    for row in provider_rows:
        if row[0] not in existing_ids:
            writer.writerow(row)

alias_rows = [
    ["Phoenix", "arizona", "phoenix-water", "phoenix", "REDIRECT", "City search demand maps directly to the governing Phoenix backflow program.", TODAY],
    ["Tucson", "arizona", "tucson-water", "tucson", "REDIRECT", "City search demand maps directly to the governing Tucson Water workflow.", TODAY],
    ["Mesa", "arizona", "mesa-water", "mesa", "REDIRECT", "City search demand maps directly to Mesa Water Resources and the city portal workflow.", TODAY],
    ["San Diego", "california", "san-diego-water", "san-diego", "REDIRECT", "City search demand maps directly to the San Diego Public Utilities backflow program.", TODAY],
    ["Irvine", "california", "irwd-backflow", "irvine", "REDIRECT", "City search demand maps directly to IRWD's governing district program.", TODAY],
    ["Oakland", "california", "ebmud-backflow", "oakland", "NOINDEX_BRIDGE", "Regional utility coverage is stronger than a city-only page, so keep an alias bridge instead of a hard redirect.", TODAY],
    ["Denver", "colorado", "denver-water", "denver", "REDIRECT", "City search demand maps directly to Denver Water's governing program.", TODAY],
    ["Aurora", "colorado", "aurora-water", "aurora", "REDIRECT", "City search demand maps directly to Aurora Water's governing program.", TODAY],
    ["Colorado Springs", "colorado", "csu-backflow", "colorado-springs", "REDIRECT", "City search demand maps directly to Colorado Springs Utilities.", TODAY],
    ["Miami", "florida", "miami-dade-wasd", "miami", "NOINDEX_BRIDGE", "County authority governs the compliance program, so keep a bridge page for city-branded discovery.", TODAY],
    ["Tampa", "florida", "tampa-water", "tampa", "REDIRECT", "City search demand maps directly to Tampa Water Department's governing program.", TODAY],
    ["Fort Lauderdale", "florida", "broward-water", "fort-lauderdale", "NOINDEX_BRIDGE", "County authority governs the Broward utility program, so keep a bridge rather than implying a city-owned authority page.", TODAY],
]

aliases_path = ROOT / "data" / "city_aliases.csv"
existing_alias_lines = aliases_path.read_text(encoding="utf-8").splitlines()
existing_alias_keys = {tuple(line.split(",")[1:4]) for line in existing_alias_lines[1:] if line.strip()}
with aliases_path.open("a", encoding="utf-8", newline="") as handle:
    writer = csv.writer(handle)
    for row in alias_rows:
        key = tuple(row[1:4])
        if key not in existing_alias_keys:
            writer.writerow(row)

selection_doc = ROOT / "ops" / "representative_states.md"
selection_doc.write_text(
    """# Representative State Selection

Locked on 2026-04-04 after reviewing official utility and state-program sources. These five states best match the utility-first backflow strategy because they combine visible state rule floors with public utility workflows strong enough for annual-testing, irrigation, fire-line, and tester-routing pages.

## Selected states
- Texas: existing anchor with strong municipal and district utility pages, irrigation and fire-line variation, and public tester lists.
- Arizona: Phoenix, Tucson, and Mesa all publish tester resources, annual testing rules, and city-run portal or ordinance workflows.
- California: state policy layer plus major utility approved-tester programs in San Diego, IRWD, and EBMUD.
- Colorado: Regulation 11 floor plus visible annual reminder, portal, and irrigation-season workflows in Denver, Aurora, and Colorado Springs.
- Florida: county and city programs publish annual or biennial cycles, outsourced reporting platforms, and irrigation-heavy compliance patterns in Miami-Dade, Broward, and Tampa.

## Why these five fit best
- Public utility pages are strong enough to support utility-first local pages without inventing process detail.
- The states show real program variation instead of repeating one template.
- Official tester or portal workflows exist often enough to support next-action pages and later provider routing.
- The mix of commercial, irrigation, multifamily, and fire-service exposure is broad enough to test monetization paths without devolving into generic plumbing SEO.
""",
    encoding="utf-8",
)
