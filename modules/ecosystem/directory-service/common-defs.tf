#
# Definitions shared across terraform recipes
#

provider "docker" {
  version = "~> 0.1"
}

variable "sambaAdminPassword" {
  # Set this in terraform.tfvars and it will get automatically included
  description = "Administrator for AD DC"
}
