remote_state {
  backend = "s3"
  generate = {
    path = "backend.tf"
    if_exists = "overwrite"
  }
  config = {
    bucket = "wv-vc-state"
    key = "terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "wv-tfstate-lock"
  }
}
