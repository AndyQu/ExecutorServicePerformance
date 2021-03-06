/**
 * 监控一个进程的所有线程status文件
 * Ubuntu Server  16.04.1 LTS上测试通过
 * @return
 */
def pid=args[0]
File folder=new File("/proc/${pid}/task/")
new File("/tmp/${pid}/").mkdir()
while(folder.exists()){
	folder.eachFile{
	    it->
			def src=new File("/proc/${pid}/task/${it.getName()}/status")
			def target=new File("/tmp/${pid}/${it.getName()}_status.log")
			 target<< src.text
			 println "copy done: ${src} -> ${target} "
			 
			 target=new File("/tmp/${pid}/${it.getName()}_final_status.log")
			 target.write("")
			 target<<src.text
			 println "copy done: ${src} -> ${target} "
	}
	Thread.sleep(1*1000)
}
println "process stopped:${folder}"