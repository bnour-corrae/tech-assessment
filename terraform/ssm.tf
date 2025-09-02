module "db_url" {
  source = "terraform-aws-modules/ssm-parameter/aws"

  name  = "DB_URL"
  value = "placeholder"

  ignore_value_changes = true
}

module "db_user" {
  source = "terraform-aws-modules/ssm-parameter/aws"

  name  = "DB_USER"
  value = "placeholder"

  ignore_value_changes = true
}

module "db_password" {
  source = "terraform-aws-modules/ssm-parameter/aws"

  name  = "DB_PASSWORD"
  value = "placeholder"

  ignore_value_changes = true
}
