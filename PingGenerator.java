import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 

public class PingGenerator implements Callable<String>{
    private String execCmd( String cmd ){
        StringBuffer output = new StringBuffer();

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";           
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return output.toString();
    }

     @Override
    public String call() throws Exception {
        // get cmd output
        return execCmd("ping -o localhost");
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> list = new ArrayList<Future<String>>();
        Callable<String> callable = new PingGenerator();
        
        for(int i=0; i< 3; i++){
            Future<String> future = executor.submit(callable);
            list.add(future);
        }
        for(Future<String> fut : list){
            try {
               
                System.out.println(new Date()+ "::"+fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
} 

