data "aws_iam_policy_document" "github_action_repo_access" {
  statement {
    effect = "Allow"
    actions = [
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "ecr:GetAuthorizationToken"
    ]
    resources = ["*"]
  }
}

module "ecr_push_policy" {
  source = "terraform-aws-modules/iam/aws//modules/iam-policy"

  name        = "GithubAccessEcr"
  path        = "/"
  description = "Allows Github to push to ECR"

  policy = data.aws_iam_policy_document.github_action_repo_access.json
}

data "aws_iam_policy_document" "github_actions_ecs_access" {
  statement {
    effect = "Allow"
    actions = [
      "ecs:RegisterTaskDefinition",
      "ecs:DescribeTaskDefinition"
    ]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "iam:PassRole"
    ]
    resources = [
      "arn:aws:iam::${data.aws_caller_identity.this.account_id}:role/${local.app_name}-*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "ecs:UpdateService",
      "ecs:DescribeServices"
    ]
    resources = [
      "arn:aws:ecs:${local.region}:${data.aws_caller_identity.this.account_id}:service/${local.ecs_cluster_name}/${local.app_name}"
    ]
  }
}

module "ecs_push_policy" {
  source = "terraform-aws-modules/iam/aws//modules/iam-policy"

  name        = "GithubAccessEcs"
  path        = "/"
  description = "Allows Github to push to ECS"

  policy = data.aws_iam_policy_document.github_actions_ecs_access.json
}

module "oidc_provider" {
  source = "github.com/philips-labs/terraform-aws-github-oidc//modules/provider"
}

module "oidc_github_actions" {
  source = "github.com/philips-labs/terraform-aws-github-oidc?ref=v0.8.1"

  openid_connect_provider_arn = module.oidc_provider.openid_connect_provider.arn
  repo                        = "${local.github_repo_name}/${local.app_name}"
  role_name                   = "GithubActionsAccess"
  role_policy_arns = [
    module.ecr_push_policy.arn,
    module.ecs_push_policy.arn
  ]
  conditions = [{
    test     = "StringLike"
    variable = "token.actions.githubusercontent.com:sub"
    values   = ["repo:${local.github_repo_name}/${local.app_name}:*"]
  }]
}
