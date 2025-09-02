module "ecs" {
  source = "terraform-aws-modules/ecs/aws"

  cluster_name = local.ecs_cluster_name

  default_capacity_provider_strategy = {
    FARGATE = {
      weight = 50
      base   = 20
    }
    FARGATE_SPOT = {
      weight = 50
    }
  }

  create_task_exec_iam_role = true
  create_task_exec_policy   = true

  create_cloudwatch_log_group = true
}

module "service" {
  source = "terraform-aws-modules/ecs/aws//modules/service"

  name        = local.app_name
  cluster_arn = module.ecs.cluster_arn

  cpu    = 1024
  memory = 2048

  enable_execute_command = true

  enable_autoscaling       = true
  autoscaling_max_capacity = 3
  autoscaling_min_capacity = 1
  assign_public_ip         = true

  requires_compatibilities = [
    "FARGATE"
  ]

  container_definitions = {
    vehicle-catalog = {
      family    = local.ecs_cluster_name
      essential = true
      image     = "nginx:bookworm@sha256:33e0bbc7ca9ecf108140af6288c7c9d1ecc77548cbfd3952fd8466a75edefe57"
      portMappings = [
        {
          name          = "default"
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]
      enable_cloudwatch_logging   = true
      create_cloudwatch_log_group = true
      logConfiguration = {
        logDriver = "awslogs"
      }
    }
  }

  load_balancer = {
    service = {
      target_group_arn = module.alb.target_groups["ex_ecs"].arn
      container_name   = local.app_name
      container_port   = 8080
    }
  }

  security_group_ingress_rules = {
    alb_8080 = {
      description                  = "Service port"
      from_port                    = local.container_port
      ip_protocol                  = "tcp"
      referenced_security_group_id = module.alb.security_group_id
    }
  }

  security_group_egress_rules = {
    all = {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  }

  ignore_task_definition_changes = true

  subnet_ids = module.vpc.private_subnets
}
