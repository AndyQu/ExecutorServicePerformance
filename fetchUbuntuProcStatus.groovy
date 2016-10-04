def pid=args[0]
File folder=new File("/proc/${pid}/task/")
new File("/tmp/${pid}/").deleteDir()
new File("/tmp/${pid}/").mkdir()
folder.eachFile{
    it->new File("/tmp/${pid}/${it.getName()}_status.log") << new File("/proc/${pid}/task/${it.getName()}/status").text
}