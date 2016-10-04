Double M=1000
def stat=[]
stat.add(["N", "count", "value"])
for(int N=1;N<=M*1.2;N++){
    double count=Math.ceil(M/N)
    double value=count*N
    stat.add([N,count,value])
}

def outputF=new File("/tmp/threads_performance.csv")
outputF.write ""
stat.each{
    it->
        outputF.append it.join(",")
        outputF.append "\n"
}
