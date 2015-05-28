import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.lang.*;
import java.util.concurrent.*;
/**
* Sample code for async parallel workers using Executors & Callables. 
*/

public class ParallelScrapper{
    public static ExecutorService executor = Executors.newFixedThreadPool(500);
   
    public static void main(String[] args) throws InterruptedException, ExecutionException{
        ScrapeUrls( Arrays.asList(
            "http://www.liveclicker.com/",
            "https://www.facebook.com/",
            "https://twitter.com/"));

        executor.shutdown();
    }

    // go thru supplied urls, download them and count resources (using subworkers)
    private static void ScrapeUrls(List<String> urls) throws InterruptedException, ExecutionException{
        List<Future<ResourceStats>> workers = new ArrayList<Future<ResourceStats>>();
        List<Callable<ResourceStats>> callables = new ArrayList<Callable<ResourceStats>>();
        // submit work to be executed asynchronously
        for(String url : urls ){
            Callable<ResourceStats> scrapper = new ResourceStatsScrapper( url );
            callables.add( scrapper );
            //Future<ResourceStats> future = executor.submit(scrapper);
           // workers.add(future);
        }

        List<Future<ResourceStats>> futures = executor.invokeAll(callables);

        for(Future<ResourceStats> future : futures){
            System.out.println(future.get());
        }
    }
} 

// Responsible for downloading a url content and dividing/delegating work by resource type
class ResourceStatsScrapper implements Callable<ResourceStats>{
    private String sourceUrl;

    public ResourceStatsScrapper(String url ){
        this.sourceUrl = url;
    }

    @Override
    public ResourceStats call() throws Exception {
        try{
            return doScrape( this.sourceUrl );
        }catch( IOException ex ){
            ex.printStackTrace();
        }catch( InterruptedException ex ){
            ex.printStackTrace();
        }catch( ExecutionException ex ){
            ex.printStackTrace();
        }

        return new ResourceStats( this.sourceUrl );
    }

    private String getPageAsString( URL url )throws IOException{
        System.out.println("Retrieving url "+ url.toString() );
        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        int numCharsRead;
        char[] charArray = new char[1024];
        StringBuffer sb = new StringBuffer();

        while ((numCharsRead = isr.read(charArray)) > 0) {
            sb.append(charArray, 0, numCharsRead);
        }

        System.out.println("Retrieved url "+ url.toString() +" page size["+ sb.length() +"]" );
        return sb.toString();
    }
   
    private ResourceStats doScrape( String url)throws IOException, InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        ResourceStats stats = new ResourceStats(url);
        URL urlObj = new URL(url);
        String content = getPageAsString( urlObj );

        Callable<Integer> cssCounter = new ResourceCounter( content, ".*\\.css.*" );
        Future<Integer> cssFuture = ParallelScrapper.executor.submit(cssCounter);

        Callable<Integer> jsCounter = new ResourceCounter( content, ".*\\.js.*" );
        Future<Integer> jsFuture = ParallelScrapper.executor.submit(jsCounter);

        Callable<Integer> imgCounter = new ResourceCounter( content,"(.*\\.png.*|.*\\.jpg.*|\\.gif.*)" );
        Future<Integer> imgFuture = ParallelScrapper.executor.submit(imgCounter);

        System.out.println("Waiting for all resource counter to return for url["+ url +"]");
        stats.setCssCount( cssFuture.get() );
        stats.setImgCount( imgFuture.get() );
        stats.setJsCount( jsFuture.get() );
        System.out.println("All resource counters returned for url["+ url +"]");
        stats.setExecTime( System.currentTimeMillis() - start);

        return stats;
    }
}

/**
* Responsible for extracting the count of a resource identified by pattern supplied
* in constructor.
*/
class ResourceCounter implements Callable<Integer>{
    private String source;
    private String pattern;
    private Pattern patternMatcher; 

    ResourceCounter( String content, String pattern ){
        this.source = content;
        this.pattern = pattern;
        this.patternMatcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE );
    }

    @Override
    public Integer call() throws Exception {
        int count = 0;
        Matcher matcher = patternMatcher.matcher(this.source);
        
        while( matcher.find() ){
            count++;
        }  

        //System.out.println("ResourceCounter ["+ this.pattern +"] count["+ count +"]" );

        return new Integer(count);
    }
}

// holds resource counts of a given source url
class ResourceStats{ 
    private String sourceUrl;
    private int cssCount;
    private int imgCount;
    private int jsCount;
    private long execTime;

    public ResourceStats( String sourceUrl ){
        this.sourceUrl = sourceUrl;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Source:");
        sb.append( sourceUrl );
        sb.append("\n");
        sb.append("Stats: css[");
        sb.append( cssCount );
        sb.append("] img[");
        sb.append( imgCount );
        sb.append("] js[");
        sb.append( jsCount );
        sb.append("] \n");
        sb.append("Execution time (ms)[");
        sb.append( execTime );
        sb.append("]\n");

        return sb.toString();
    }

    public String getSourceUrl(){
        return this.sourceUrl;
    }
    public void setImgCount( int count ){
        this.imgCount = count;
    }
    public void setCssCount( int count ){
        this.cssCount = count;
    }
    public void setJsCount( int count ){
        this.jsCount = count;
    }
    public void setExecTime( long execTime ){
        this.execTime = execTime;
    }
}

