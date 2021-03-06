package joss.jacobo.lol.lcs.api;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import joss.jacobo.lol.lcs.R;
import joss.jacobo.lol.lcs.api.model.Config;
import joss.jacobo.lol.lcs.api.model.LiveStreams.Video;
import joss.jacobo.lol.lcs.api.model.Liveticker.*;
import joss.jacobo.lol.lcs.api.model.Liveticker.Error;
import joss.jacobo.lol.lcs.api.model.News.News;
import joss.jacobo.lol.lcs.api.model.Players.Player;
import joss.jacobo.lol.lcs.api.model.Replays.Replay;
import joss.jacobo.lol.lcs.api.model.Standings.Standings;
import joss.jacobo.lol.lcs.model.NewsModel;
import joss.jacobo.lol.lcs.model.TweetsModel;
import joss.jacobo.lol.lcs.provider.matches.MatchesColumns;
import joss.jacobo.lol.lcs.provider.matches.MatchesContentValues;
import joss.jacobo.lol.lcs.provider.news.NewsColumns;
import joss.jacobo.lol.lcs.provider.news.NewsContentValues;
import joss.jacobo.lol.lcs.provider.players.PlayersColumns;
import joss.jacobo.lol.lcs.provider.players.PlayersContentValues;
import joss.jacobo.lol.lcs.provider.replays.ReplaysColumns;
import joss.jacobo.lol.lcs.provider.replays.ReplaysContentValues;
import joss.jacobo.lol.lcs.provider.standings.StandingsColumns;
import joss.jacobo.lol.lcs.provider.standings.StandingsContentValues;
import joss.jacobo.lol.lcs.provider.team_details.TeamDetailsColumns;
import joss.jacobo.lol.lcs.provider.team_details.TeamDetailsContentValues;
import joss.jacobo.lol.lcs.provider.teams.TeamsColumns;
import joss.jacobo.lol.lcs.provider.teams.TeamsContentValues;
import joss.jacobo.lol.lcs.provider.tournaments.TournamentsColumns;
import joss.jacobo.lol.lcs.provider.tournaments.TournamentsContentValues;
import joss.jacobo.lol.lcs.provider.tweets.TweetsColumns;
import joss.jacobo.lol.lcs.provider.tweets.TweetsContentValues;
import joss.jacobo.lol.lcs.provider.tweets.TweetsSelection;
import joss.jacobo.lol.lcs.utils.GGson;
import joss.jacobo.lol.lcs.utils.Twitter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import twitter4j.ResponseList;
import twitter4j.Status;

public class ApiService extends IntentService {

    public static final String BROADCAST = "ApiService.broadcast";
    private static final String TAG = "ApiService";

    public static final String API_TYPE = "api_type";
    public static final String MESSAGE = "message";
    public static final String TWITTER_HANDLE = "twitter_handle";
    public static final String TEAM_ID = "team_id";
    public static final String NUM_OF_ARTICLES = "num_of_articles";
    public static final String OFFSET = "offset";
    public static final String HASH_TAG_LCS = "#LCS";
    public static final String LIVESTREAM_VIDEOS = "livestream_videos";
    public static final String REPLAYS = "replays";
    public static final String NUM_OF_REPLAYS = "num_of_replays";
    public static final String EVENT_ID = "event_id";
    public static final String LIVETICKER_EVENTS = "liveticker_events";

    public static final int TYPE_INITIAL_CONFIG = 0;
    public static final int TYPE_LATEST_STANDINGS = 1;
    public static final int TYPE_GET_TWEETS = 2;
    public static final int TYPE_GET_PLAYERS = 3;
    public static final int TYPE_NEWS = 4;
    public static final int TYPE_GET_TWEETS_LCS = 5;
    public static final int TYPE_GET_LIVESTREAM_VIDEOS = 6;
    public static final int TYPE_GET_REPLAYS = 7;
    public static final int TYPE_GET_LIVETICKER_EVENTS = 8;

    public static final String STATUS = "status";
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    RestService service;
    LocalBroadcastManager broadcast;

    public ApiService() {
        super("Api Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://lcs.jossay.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
            .build();
        service = restAdapter.create(RestService.class);
        broadcast = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch(intent.getExtras().getInt(API_TYPE)){
            case TYPE_INITIAL_CONFIG:
                service.getInitialConfig(new Callback<Config>() {
                    @Override
                    public void success(Config config, Response response) {
                        updateInitialConfig(getContentResolver(), config);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "onFailure");
                    }
                });
                break;

            case TYPE_LATEST_STANDINGS:
                service.getLatestStandings(new Callback<Standings>() {
                    @Override
                    public void success(Standings standings, Response response) {
                        updateLatestStandings(getContentResolver(), standings);
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                break;

            case TYPE_GET_TWEETS:
                final String twitterHandle = intent.getStringExtra(TWITTER_HANDLE);
                Twitter.getUsersTimeline(twitterHandle, new Twitter.TwitterCallback() {
                    @Override
                    public void onSuccess(ResponseList<Status> statuses) {
                        updateTweets(getContentResolver(), twitterHandle, statuses);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                });
                break;

            case TYPE_GET_TWEETS_LCS:
                Twitter.getLCSTweets(new Twitter.TwitterHashTagCallback() {
                    @Override
                    public void onSuccess(List<Status> statuses) {
                        insertTweets(getContentResolver(), statuses);
                        sendHashTagTweetsBroadcast(SUCCESS);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        sendHashTagTweetsBroadcast(ERROR);
                    }
                });
                break;

            case TYPE_GET_PLAYERS:
                service.getPlayers(intent.getIntExtra(TEAM_ID, -1), new Callback<List<Player>>() {
                    @Override
                    public void success(List<Player> players, Response response) {
                        updatePlayers(getContentResolver(), players);

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                break;

            case TYPE_NEWS:
                int numOfArticles = intent.getIntExtra(NUM_OF_ARTICLES, 5);
                final int offset = intent.getIntExtra(OFFSET, 0);
                service.getNews(numOfArticles, offset, new Callback<List<News>>() {
                    @Override
                    public void success(List<News> newses, Response response) {
                        if(offset == 0){
                            updateNews(getContentResolver(), newses);
                        }else{
                            insertNews(getContentResolver(), newses);
                        }

                        sendNewsBroadcast(SUCCESS);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        sendNewsBroadcast(ERROR);
                    }
                });
                break;

            case TYPE_GET_LIVESTREAM_VIDEOS:
                service.getLiveStreamVideos(new Callback<List<Video>>() {
                    @Override
                    public void success(List<Video> videos, Response response) {
                        sendLiveStreamBroadcast(videos, SUCCESS);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        sendLiveStreamBroadcast(null, ERROR);
                    }
                });
                break;

            case TYPE_GET_REPLAYS:
                int numOfReplays = intent.getIntExtra(NUM_OF_REPLAYS, 5);
                final int replaysOffset = intent.getIntExtra(OFFSET, 0);
                service.getReplays(numOfReplays, replaysOffset, new Callback<List<Replay>>() {
                    @Override
                    public void success(List<Replay> replays, Response response) {
                        if(replaysOffset == 0){
                            updateReplays(getContentResolver(), replays);
                        }else{
                            insertReplays(getContentResolver(), replays);
                        }

                        sendReplaysBroadcast(SUCCESS, replays);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        sendReplaysBroadcast(ERROR, null);
                    }
                });
                break;

            case TYPE_GET_LIVETICKER_EVENTS:
                service.getLivetickerEvents(intent.getIntExtra(EVENT_ID, 0), new Callback<Liveticker>() {
                    @Override
                    public void success(Liveticker liveticker, Response response) {
                        sendLivetickerEventsBroadcast(liveticker, SUCCESS, null);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Error livetickerError = null;
                        if (!error.isNetworkError() && error.getResponse() != null) {
                            livetickerError = (Error) error.getBodyAs(Error.class);
                        }
                        sendLivetickerEventsBroadcast(null, ERROR,
                                livetickerError != null
                                        ? livetickerError.message : getString(R.string.live_streaming_error_starting_stream));
                    }
                });
                break;
        }
    }

    /**
     * API Calls
     */
    public static void getInitialConfig(Context context){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_INITIAL_CONFIG);
        context.startService(intent);
    }

    public static void getLatestStandings(Context context){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_LATEST_STANDINGS);
        context.startService(intent);
    }

    public static void getTweets(Context context, String twitterHandle){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_TWEETS);
        intent.putExtra(ApiService.TWITTER_HANDLE, twitterHandle);
        context.startService(intent);
    }

    public static void getLCSTweets(Context context){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_TWEETS_LCS);
        context.startService(intent);
    }

    public static void getPlayers(Context context, int teamId){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_PLAYERS);
        intent.putExtra(ApiService.TEAM_ID, teamId);
        context.startService(intent);
    }

    public static void getNews(Context context, int numOfArticles, int offset){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_NEWS);
        intent.putExtra(ApiService.NUM_OF_ARTICLES, numOfArticles);
        intent.putExtra(ApiService.OFFSET, offset);
        context.startService(intent);
    }

    public static void getLiveStreamVideos(Context context){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_LIVESTREAM_VIDEOS);
        context.startService(intent);
    }

    public static void getReplays(Context context, int numOfReplays, int offset){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_REPLAYS);
        intent.putExtra(ApiService.NUM_OF_REPLAYS, numOfReplays);
        intent.putExtra(ApiService.OFFSET, offset);
        context.startService(intent);
    }

    public static void getLivetickerEvents(Context context, int eventId){
        Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(ApiService.API_TYPE, ApiService.TYPE_GET_LIVETICKER_EVENTS);
        intent.putExtra(ApiService.EVENT_ID, eventId);
        context.startService(intent);
    }

    /**
     * DB updates
     */
    public static void updateInitialConfig(ContentResolver contentResolver, Config initalConfig) {

        contentResolver.delete(TournamentsColumns.CONTENT_URI, null, null);
        ContentValues[] tournamentCV = TournamentsContentValues.getContentValues(initalConfig.getTournaments());
        contentResolver.bulkInsert(TournamentsColumns.CONTENT_URI, tournamentCV);

        contentResolver.delete(TeamDetailsColumns.CONTENT_URI, null, null);
        ContentValues[] teamDetailsCV = TeamDetailsContentValues.getContentValues(initalConfig.getTeamDetails());
        contentResolver.bulkInsert(TeamDetailsColumns.CONTENT_URI, teamDetailsCV);

        contentResolver.delete(TeamsColumns.CONTENT_URI, null, null);
        ContentValues[] teamsCV= TeamsContentValues.getContentValues(initalConfig.getTeams());
        contentResolver.bulkInsert(TeamsColumns.CONTENT_URI, teamsCV);

        contentResolver.delete(MatchesColumns.CONTENT_URI, null, null);
        ContentValues[] matchesCV = MatchesContentValues.getContentValues(initalConfig.getMatches());
        contentResolver.bulkInsert(MatchesColumns.CONTENT_URI, matchesCV);

        contentResolver.delete(PlayersColumns.CONTENT_URI, null, null);
        ContentValues[] playersCV = PlayersContentValues.getContentValues(initalConfig.getPlayers());
        contentResolver.bulkInsert(PlayersColumns.CONTENT_URI, playersCV);
    }

    public static void updateLatestStandings(ContentResolver contentResolver, Standings standings) {
        contentResolver.delete(StandingsColumns.CONTENT_URI, null, null);
        ContentValues[] standingsCV = StandingsContentValues.getContentValues(standings.getList());
        contentResolver.bulkInsert(StandingsColumns.CONTENT_URI, standingsCV);
    }

    private void updateTweets(ContentResolver contentResolver, String twitterHandle, ResponseList<Status> statuses) {

        TweetsSelection where = new TweetsSelection();
        where.twitterHandle(twitterHandle);

        contentResolver.delete(TweetsColumns.CONTENT_URI, where.sel(), where.args());
        ContentValues[] tweetsValues = TweetsContentValues.getContentValues(TweetsModel.getList(statuses));
        contentResolver.bulkInsert(TweetsColumns.CONTENT_URI, tweetsValues);
    }

    private void insertTweets(ContentResolver contentResolver, List<Status> statuses) {
        ContentValues[] tweetsValues = TweetsContentValues.getContentValues(TweetsModel.getList(statuses));
        contentResolver.bulkInsert(TweetsColumns.CONTENT_URI, tweetsValues);
    }

    public static void deleteHashTagTweets(ContentResolver contentResolver){
        TweetsSelection where = new TweetsSelection();
        where.textLike("%" + ApiService.HASH_TAG_LCS + "%");
        contentResolver.delete(TweetsColumns.CONTENT_URI, where.sel(), where.args());
    }

    private void updatePlayers(ContentResolver contentResolver, List<Player> players) {
        ContentValues[] playersValues = PlayersContentValues.getContentValues(Player.getList(players));
        contentResolver.bulkInsert(PlayersColumns.CONTENT_URI, playersValues);
    }

    private void insertNews(ContentResolver contentResolver, List<News> newses){
        ContentValues[] newsCV = NewsContentValues.getContentValues(NewsModel.getList(newses));
        contentResolver.bulkInsert(NewsColumns.CONTENT_URI, newsCV);
    }

    private void updateNews(ContentResolver contentResolver, List<News> newses){
        contentResolver.delete(NewsColumns.CONTENT_URI, null, null);
        ContentValues[] newsCV = NewsContentValues.getContentValues(NewsModel.getList(newses));
        contentResolver.bulkInsert(NewsColumns.CONTENT_URI, newsCV);
    }

    private void insertReplays(ContentResolver contentResolver, List<Replay> replays){
        ContentValues[] replaysCV = ReplaysContentValues.getContentValues(Replay.getList(replays));
        contentResolver.bulkInsert(ReplaysColumns.CONTENT_URI, replaysCV);
    }

    private void updateReplays(ContentResolver contentResolver, List<Replay> replays){
        contentResolver.delete(ReplaysColumns.CONTENT_URI, null, null);
        ContentValues[] replaysCV = ReplaysContentValues.getContentValues(Replay.getList(replays));
        contentResolver.bulkInsert(ReplaysColumns.CONTENT_URI, replaysCV);
    }

    /**
     * Send Broadcasts
     */
    private void sendHashTagTweetsBroadcast(int status) {
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(API_TYPE, TYPE_GET_TWEETS_LCS);
        intent.putExtra(STATUS, status);
        broadcast.sendBroadcast(intent);
    }

    private void sendLiveStreamBroadcast(List<Video> videos, int status) {
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(API_TYPE, TYPE_GET_LIVESTREAM_VIDEOS);
        intent.putExtra(STATUS, status);

        if(status == SUCCESS){
            intent.putExtra(LIVESTREAM_VIDEOS, GGson.toJson(videos));
        }

        broadcast.sendBroadcast(intent);
    }

    private void sendNewsBroadcast(int status){
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(API_TYPE, TYPE_NEWS);
        intent.putExtra(STATUS, status);
        broadcast.sendBroadcast(intent);
    }

    private void sendReplaysBroadcast(int status, List<Replay> replays) {
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(API_TYPE, TYPE_GET_REPLAYS);
        intent.putExtra(STATUS, status);

        if(status == SUCCESS && replays != null){
            intent.putExtra(REPLAYS, GGson.toJson(replays));
        }

        broadcast.sendBroadcast(intent);
    }

    private void sendLivetickerEventsBroadcast(Liveticker liveticker, int status, String message){
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(API_TYPE, TYPE_GET_LIVETICKER_EVENTS);
        intent.putExtra(STATUS, status);

        if(status == SUCCESS){
            intent.putExtra(LIVETICKER_EVENTS, GGson.toJson(liveticker));
        }else{
            intent.putExtra(MESSAGE, message);
        }

        broadcast.sendBroadcast(intent);
    }

}