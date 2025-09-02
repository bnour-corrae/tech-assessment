variable "db_password" {
  type = string
  description = "The password of the database user. DON'T SET THIS IN ANY FILE! Set it as an environment variable in your terminal as TF_VAR_db_password."
}
