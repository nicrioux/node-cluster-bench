# -*- mode: ruby -*-
# vi: set ft=ruby :

require 'yaml'

current_dir    = File.dirname(File.expand_path(__FILE__))
configs        = YAML.load_file("#{current_dir}/config.yaml")
vagrant_config = configs['configs']

Vagrant.configure(2) do |config|
  config.vm.box = "digital_ocean"
  config.ssh.private_key_path = "~/.ssh/id_rsa"
  
  config.vm.provider :digital_ocean do |provider|
      provider.token = vagrant_config["do_api_key"]
      provider.image = "docker"
      provider.region = "sfo1"
      provider.size = "8gb"  # 2gb 8gb 16gb
    end

  config.vm.define "client" do |client|
    client.vm.box = "digital_ocean"
    client.vm.provision "shell", path: "client-setup.sh"
  end

  config.vm.define "api" do |api|
    api.vm.box = "digital_ocean"
    api.vm.provision "shell", path: "api-setup.sh"
  end
end
