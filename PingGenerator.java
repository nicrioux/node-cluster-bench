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

    @Override
    public String call() throws Exception {
        return execPing();
    }
    /**
    * Execute ping and return its output.
    */
    private String execPing(){
        StringBuffer output = new StringBuffer();

        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 localhost");
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

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> workers = new ArrayList<Future<String>>();
        Callable<String> pinger = new PingGenerator();
        
        // submit work to be executed asynchronously
        for(int i=0; i< 3; i++){
            Future<String> future = executor.submit(pinger);
            workers.add(future);
        }

        // wait for all to complete and print each's output to stdout
        for(Future<String> fut : workers){
            try {
                System.out.println(fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
} 

