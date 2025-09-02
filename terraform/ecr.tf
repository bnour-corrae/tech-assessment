module "ecr" {
  source = "terraform-aws-modules/ecr/aws"
  repository_name = "wv-ecr" # update the policy document if this changes

  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last 30 images",
        selection = {
          tagStatus     = "tagged",
          tagPrefixList = ["v"],
          countType     = "imageCountMoreThan",
          countNumber   = 30
        },
        action = {
          type = "expire"
        }
      }
    ]
  })
}

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
    resources = ["arn:aws:ecr:us-east-1:${data.aws_caller_identity.this.account_id}:repo/*"]
  }
}

module "ecr_push_policy" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-policy"

  name        = "github-access-ecr"
  path        = "/"
  description = "Allows Github to push to ECR"

  policy = data.aws_iam_policy_document.github_action_repo_access.json
}

module "oidc_provider" {
  source = "github.com/philips-labs/terraform-aws-github-oidc//modules/provider"
}

module "oidc_github_actions" {
  source = "github.com/philips-labs/terraform-aws-github-oidc?ref=v0.8.1"

  openid_connect_provider_arn = module.oidc_provider.openid_connect_provider.arn
  repo                        = "bnour-corrae/tech-assessment"
  role_name                   = "github-access"
  role_policy_arns = [
    module.ecr_push_policy.arn
  ]
}
