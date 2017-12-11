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

variable "docker_default_network" {
  description = "Name of default docker network"
  default = "bridge"
}

variable "sambaDomain" {
  description = "Domain name"
  default = "SAVIOR.LOCAL"
}
