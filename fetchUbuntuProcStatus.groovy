def pid=args[0]
File folder=new File("/proc/${pid}/task/")
new File("/tmp/${pid}/").mkdir()
while(folder.exists()){
	folder.eachFile{
	    it->new File("/tmp/${pid}/${it.getName()}_status.log") < new File("/proc/${pid}/task/${it.getName()}/status").text
	}
	Thread.sleep(1*1000)
}