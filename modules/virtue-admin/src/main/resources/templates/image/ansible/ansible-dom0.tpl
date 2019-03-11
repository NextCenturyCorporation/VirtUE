cd /home/ubuntu/virtue-ansible
mkdir -p image-creator 
echo [dom0-image] > image-creator/${initFile}
echo ${xenVm.internalHostname} >> image-creator/${initFile}
cat image-creator/${initFile}
ls -alh image-creator/${initFile}
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i image-creator/${initFile} -e 'host_key_checking=False' virtue-create-xen-dom0.yml | tee image-creator/${initFile}.log

rm image-creator/${initFile}*
