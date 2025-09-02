module "db_sg" {
  source = "terraform-aws-modules/security-group/aws"

  name        = "${local.app_name}-db-sg"
  description = "Security group for database to receive connection from the cluster"
  vpc_id      = module.vpc.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 5432
      to_port     = 5432
      protocol    = "tcp"
      description = "PostgreSQL access from within VPC"
      cidr_blocks = module.vpc.vpc_cidr_block
    }
  ]
}

module "db" {
  source = "terraform-aws-modules/rds/aws"

  identifier = "${local.app_name}-db"

  engine            = "postgres"
  engine_version    = "17"
  instance_class    = "db.t3.micro"
  allocated_storage = 5

  multi_az = true

  db_name  = local.database_name
  password = var.db_password
  username = local.database_name
  port     = "5432"

  vpc_security_group_ids = [module.db_sg.security_group_id]
  create_monitoring_role = true

  db_subnet_group_name = module.vpc.database_subnet_group

  family = "postgres17"
}
