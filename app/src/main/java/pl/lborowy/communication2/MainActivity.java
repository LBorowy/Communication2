package pl.lborowy.communication2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pl.lborowy.communication2.Model.MovieModel;

public class MainActivity extends AppCompatActivity {

    private final String URL_CONNECT = "https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesData.txt";

    private ListView lvMovies;


    //1 https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesDemoItem.txt
    //2 https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesDemoList.txt
    //3 https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesData.txt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvMovies = (ListView) findViewById(R.id.lvMovies);

        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTASK().execute(URL_CONNECT);
            }
        });

//                new JSONTASK().execute("https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesDemoItem.txt");
    }

    private class JSONTASK extends AsyncTask<String, String, List<MovieModel>> {

        @Override
        protected List<MovieModel> doInBackground(String... urls) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("movies");


                List<MovieModel> movieModelList = new ArrayList<>();

                for(int i=0; i<parentArray.length(); i++) {
                    MovieModel movieModel = new MovieModel();
                    JSONObject finalObject = parentArray.getJSONObject(i);

                    movieModel.setMovie(finalObject.getString("movie"));
                    movieModel.setYear(finalObject.getInt("year"));
                    movieModel.setRating((float) finalObject.getDouble("rating"));
                    movieModel.setDirector(finalObject.getString("director"));

                    movieModel.setDuration(finalObject.getString("duration"));
                    movieModel.setTagline(finalObject.getString("tagline"));
                    movieModel.setImage(finalObject.getString("image"));
                    movieModel.setStory(finalObject.getString("story"));

                    List<MovieModel.Cast> castList = new ArrayList<>();
                    for(int j=0; j<finalObject.getJSONArray("cast").length(); j++){
                        MovieModel.Cast cast = new MovieModel.Cast();
                        cast.setName(finalObject.getJSONArray("cast").getJSONObject(j).getString("name"));
                        castList.add(cast);
                    }
                    movieModel.setCastList(castList);
                    movieModelList.add(movieModel);
                }
                return movieModelList;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<MovieModel> result) {
            super.onPostExecute(result);
            if(result != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.list_layout, result);
                lvMovies.setAdapter(adapter);
            } else {
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server, please check url.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class MovieAdapter extends ArrayAdapter {

        private List<MovieModel> movieModelList;
        private int resource;
        private LayoutInflater inflater;
        public MovieAdapter(Context context, int resource, List<MovieModel> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.ivMovieIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
                holder.tvMovie = (TextView)convertView.findViewById(R.id.t1_movieName);
                holder.tvTagline = (TextView)convertView.findViewById(R.id.t1_tagLine);
                holder.tvYear = (TextView)convertView.findViewById(R.id.t1_year);
                holder.tvDuration = (TextView)convertView.findViewById(R.id.t1_duration);
                holder.tvDirector = (TextView)convertView.findViewById(R.id.t1_director);
                holder.rbMovieRating = (RatingBar)convertView.findViewById(R.id.ratingBar);
                holder.tvCast = (TextView)convertView.findViewById(R.id.t2_tvCast);
                holder.tvStory = (TextView)convertView.findViewById(R.id.t2_story);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Then later, when you want to display image
            final ViewHolder finalHolder = holder;

            holder.tvMovie.setText(movieModelList.get(position).getMovie());
            holder.tvTagline.setText(movieModelList.get(position).getTagline());
            holder.tvYear.setText("Year: " + movieModelList.get(position).getYear());
            holder.tvDuration.setText("Duration:" + movieModelList.get(position).getDuration());
            holder.tvDirector.setText("Director:" + movieModelList.get(position).getDirector());
            Picasso.with(getContext()).load(movieModelList.get(position).getImage()).into(holder.ivMovieIcon);
            // rating bar
            holder.rbMovieRating.setRating(movieModelList.get(position).getRating()/2);

            StringBuffer stringBuffer = new StringBuffer();
            for(MovieModel.Cast cast : movieModelList.get(position).getCastList()){
                stringBuffer.append(cast.getName() + ", ");
            }

            holder.tvCast.setText("Cast:" + stringBuffer);
            holder.tvStory.setText(movieModelList.get(position).getStory());
            return convertView;
        }


        class ViewHolder{
            private ImageView ivMovieIcon;
            private TextView tvMovie;
            private TextView tvTagline;
            private TextView tvYear;
            private TextView tvDuration;
            private TextView tvDirector;
            private RatingBar rbMovieRating;
            private TextView tvCast;
            private TextView tvStory;
        }

    }


}
