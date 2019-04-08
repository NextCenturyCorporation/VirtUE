cd /home/ubuntu/virtue-ansible
mkdir -p image-creator 
echo [vhost] > image-creator/${initFile}
#echo v-domU-qcow ansible_ssh_host={domUVm.internalHostname} >> image-creator/${initFile}
#echo virtue=virtue >> image-creator/${initFile}
echo v-dom0 ansible_ssh_host=${xenVm.internalHostname} >> image-creator/${initFile}
echo v-domU ansible_ssh_host=${xenVm.internalHostname} ansible_python_interpreter=/usr/bin/python3 ansible_ssh_port=8001 >> image-creator/${initFile}

cat image-creator/${initFile}
ls -alh image-creator/${initFile}
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i image-creator/${initFile} -e 'virtue_s3_bucket="${bucket}"  encrypt_key="${encryptionKey}" virtue_s3_folder="${s3Folder}"  host_key_checking=False virtue_apps="${apps}"' virtue-install-domU-apps.yml | tee image-creator/${initFile}.log

rm image-creator/${initFile}*


