cookbook_file "/tmp/normatives.tar.gz" do
   source "normatives.tar.gz"
end

working_directory =  "/tmp"

bash "import-normatives" do
  cwd "#{working_directory}"
  code <<-EOH
    tar xvfz /tmp/normatives.tar.gz
    cd normatives/scripts/import/tosca/
    /bin/chmod +x importNormativeAll.py
    python importNormativeAll.py -i "#{node['HOST_IP']}" --debug=true > /var/lib/jetty/logs/importNormativeAll.log
  EOH
end

