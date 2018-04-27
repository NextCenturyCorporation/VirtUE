
$domain="AD.VIRTUE.NCCDO.COM"
$hostname=$args[0]
$user=$args[1]
$password=$args[2]
$keytabFile="$user.keytab"

setspn -A HTTP/$hostname $user

ktpass /out $keytabFile /mapuser $user@$domain /princ HTTP/$hostname@$domain /pass $password /ptype KRB5_NT_PRINCIPAL /crypto All