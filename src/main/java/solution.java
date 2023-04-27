import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class JobScraper implements Runnable {

    private String[] job;

    public JobScraper(String[] job) {
        this.job = job;
    }

    public void run() {

        String department = this.job[0], url = this.job[1], poster = this.job[2];

        try {
            // open connection
            InputStream inputStreamObject = new URL(url).openStream();

            // read the input stream
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
            System.out.println(jsonObject);
            String resHTML = jsonObject.getJSONObject("jobAd").getJSONObject("sections").getJSONObject("jobDescription").getString("text");
            String qualHTML = jsonObject.getJSONObject("jobAd").getJSONObject("sections").getJSONObject("qualifications").getString("text");
            String country = jsonObject.getJSONObject("location").getString("country");
            country = (country.equals("in")) ? ", India": (country.equals("id")?", Indonesia":"");
            Job jobObject = new Job(
                    solution.getTextList(resHTML),
                    solution.getTextList(qualHTML),
                    jsonObject.getString("name"),
                    jsonObject.getJSONObject("location").getString("city") + country,
                    poster
            );

            solution.addJobToDepartment(jobObject, department);

            inputStreamObject.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
public class solution {
    private static List<String[]> jobs = new ArrayList<>();
    private static Map<String, List<Job>> departmentJobs = new HashMap<>();

    public static synchronized void addJobToDepartment(Job job, String department) {
        departmentJobs.computeIfAbsent(department, k -> new ArrayList<>()).add(job);
    }

    public static List<String> getTextList(String html) {
        List<String> textList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements paragraphs = doc.select("p");
        Elements lists = doc.select("ul");
        for (Element p : paragraphs) {
            textList.add(p.text());
        }
        for (Element ul : lists) {
            for (Element li : ul.select("li")) {
                textList.add(li.text());
            }
        }
        return textList;
    }

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://www.cermati.com/karir").get();
            // get script initials
            Element element = doc.getElementById("initials");
            JSONObject jsonObject = new JSONObject(element.data());
            // get all job contents
            JSONArray allJobs = jsonObject.getJSONObject("smartRecruiterResult").getJSONObject("all").getJSONArray("content");
            for (int i = 0; i < allJobs.length(); i++) {
                String[] jobDeptURLPoster = new String[3];
                JSONObject currJob = allJobs.getJSONObject(i);
                jobDeptURLPoster[0] = currJob.getJSONObject("department").getString("label");
                System.out.println(currJob);
                // get api link
                jobDeptURLPoster[1] = currJob.getString("ref");
                try {
                    // get poster name if present
                    jobDeptURLPoster[2] = currJob.getJSONObject("creator").getString("name");
                } catch (Exception e) {
                    jobDeptURLPoster[2] = "N/A";
                }

                jobs.add(jobDeptURLPoster);
            }
            System.out.println(jobs.size());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        ExecutorService executor = Executors.newFixedThreadPool(4); // 4 threads
        for (String[] job: jobs) {
            executor.submit(new JobScraper(job));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ignored) {
        }
        System.out.println(departmentJobs);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("data.json"), departmentJobs);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
