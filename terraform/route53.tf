resource "aws_route53_zone" "this" {
  name = local.domain_name
}

module "acm" {
  source = "terraform-aws-modules/acm/aws"

  domain_name       = local.domain_name
  zone_id           = aws_route53_zone.this.id
  validation_method = "DNS"

  wait_for_validation    = true
  create_route53_records = true

  subject_alternative_names = [
    "*.${local.domain_name}"
  ]
}
