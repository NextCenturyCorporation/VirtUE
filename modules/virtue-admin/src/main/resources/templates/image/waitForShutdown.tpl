while sudo xl list | grep ${xenVm.name};
do
	#sleep 1
	echo "trying again..."
done 