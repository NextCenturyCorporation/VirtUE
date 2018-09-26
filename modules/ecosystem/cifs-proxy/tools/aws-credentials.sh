#!/usr/bin/env bash
#
# Print a bash script to set AWS credentials in environment variables
# based on what's in ~/.aws/credentials.
#
awk -f <(cat - <<'EOF'
BEGIN {
    keyVariables["aws_access_key_id"] = 1
    keyVariables["aws_secret_access_key"] = 1
}
keyVariables[$1] && $2 == "=" {
    keyValues[$1] = $3
}
END {
    for (key in keyValues) {
        printf("export %s='%s'\n", toupper(key), keyValues[key])
    }
}
EOF
    ) ~/.aws/credentials

