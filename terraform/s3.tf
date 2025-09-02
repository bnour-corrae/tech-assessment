module "vehicle_catalog_images" {
  source = "terraform-aws-modules/s3-bucket/aws"

  bucket = "${local.app_name}-images"
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  versioning = {
    enabled = true
  }

  force_destroy = true
}
