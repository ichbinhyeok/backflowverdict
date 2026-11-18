import unittest
from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path

spec = spec_from_file_location("upload_limit", Path(__file__).with_name("apply-upload-limit.py"))
module = module_from_spec(spec)
spec.loader.exec_module(module)


class UploadLimitTest(unittest.TestCase):
    def test_only_target_vhost_changes(self):
        source = 'server { server_name other.example; client_max_body_size 1m; }\nserver { server_name backflowpath.com www.backflowpath.com; location / { proxy_pass http://127.0.0.1:8093; } }'
        result, count = module.patch_config(source)
        self.assertEqual(1, count)
        self.assertIn('server_name other.example; client_max_body_size 1m;', result)
        self.assertIn('proxy_pass http://127.0.0.1:8093;', result)
        self.assertEqual(1, result.count('client_max_body_size 16m;'))

    def test_server_and_location_overrides_are_updated_idempotently(self):
        source = 'server { server_name backflowpath.com; client_max_body_size 1m; location /leads { client_max_body_size 2m; } }'
        result, _ = module.patch_config(source)
        self.assertEqual(2, result.count('client_max_body_size 16m;'))
        self.assertEqual((result, 1), module.patch_config(result))

    def test_location_only_limit_also_gets_server_default(self):
        result, _ = module.patch_config('server { server_name "backflowpath.com"; location / { client_max_body_size 1m; } }')
        self.assertEqual(2, result.count('client_max_body_size 16m;'))

    def test_comments_quotes_and_similar_domains_do_not_match(self):
        source = '# server { server_name backflowpath.com; }\nserver { server_name notbackflowpath.com; set $example "server { }"; }'
        self.assertEqual((source, 0), module.patch_config(source))

    def test_http_and_https_blocks_are_both_updated(self):
        result, count = module.patch_config('server { listen 80; server_name backflowpath.com; } server { listen 443 ssl; server_name www.backflowpath.com; }')
        self.assertEqual(2, count)
        self.assertEqual(2, result.count('client_max_body_size 16m;'))
