cd /home/ubuntu/virtue-ansible
mkdir -p image-creator 
echo [vhost] > image-creator/${initFile}
#echo v-domU-qcow ansible_ssh_host=domUVm.hostname >> image-creator/${initFile}
echo v-dom0 ansible_ssh_host=${xenVm.internalHostname} >> image-creator/${initFile}
echo v-domU ansible_ssh_host=${xenVm.internalHostname} ansible_python_interpreter=/usr/bin/python3 ansible_ssh_port=8001 >> image-creator/${initFile}
cat image-creator/${initFile}
ls -alh image-creator/${initFile}
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i image-creator/${initFile} -e "virtue_s3_bucket=${bucket} host_key_checking=False" virtue-create-baseline-domU.yml | tee image-creator/${initFile}.log

rm image-creator/${initFile}*