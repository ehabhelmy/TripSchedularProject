package com.example.romisaa.tripschedular;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingFragment extends Fragment {


    ListArrayAdapter adapter;
    SwipeMenuListView mylistView;
    ArrayList <Trip> upCommingTrips;
    DataBaseHandler handler;
    int listpostion;


    public UpcomingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler=new DataBaseHandler(getActivity());
        upCommingTrips=new ArrayList<>();
        upCommingTrips=handler.getAllUpcommingTrips();


    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Upcoming Trips");

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_upcoming, container, false);
        mylistView= (SwipeMenuListView) view.findViewById(R.id.tripNames);
        adapter=new ListArrayAdapter(view.getContext(),R.layout.singlerow2,R.id.tripName,upCommingTrips);
        mylistView.setAdapter(adapter);
        mylistView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //list view    row    element rkam kam
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(view.getContext(),ViewTrip.class);
                intent.putExtra("trip",upCommingTrips.get(position));
                intent.putExtra("note",new Notes(1,"samyNote"));
              //  System.out.println("l gy mn l upcoming "+upCommingTrips.get(position).getNotes().get(0).getContent());

                startActivity(intent);
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem startItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                startItem.setBackground(new ColorDrawable(Color.rgb(50, 205, 50)));
                // set item width
                startItem.setWidth((200));
                // set item title
                startItem.setTitle("Start");
                // set item title fontsize
                startItem.setTitleSize(18);
                startItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(startItem);


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth((200));
                // set item title
                deleteItem.setTitle("Delete");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);

                // set a icon
                //     deleteItem.setIcon(R.drawable.age);

                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        mylistView.setMenuCreator(creator);

        mylistView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
                listpostion=position;

            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });


        mylistView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // start
                        // l object m3ak ya abo 5al eml l enta 3ayzo fi
                        Trip out= (Trip) mylistView.getItemAtPosition(listpostion);
                        handler.changeStatus(out.getId(),"done");
                        TaskManager.getInstance(getActivity()).deleteTask(upCommingTrips.get(listpostion).getId());
                        upCommingTrips.remove(listpostion);
                        adapter.notifyDataSetChanged();
                        startNavigation(out);

                         //Toast.makeText(getActivity(),out.getName(),Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // delete


                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Delete");
                        alertDialog.setMessage("Do you Really want to delete that trip?! ");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Trip deleted= (Trip) mylistView.getItemAtPosition(listpostion);
                                        handler.deleteTrip(deleted.getId());
                                        TaskManager.getInstance(getActivity()).deleteTask(upCommingTrips.get(listpostion).getId());
                                        upCommingTrips.remove(listpostion);
                                        adapter.notifyDataSetChanged();

                                    }
                                });
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                        // data.removeView(data.getAdapter().getView(listpostion,null,data));

                       // Toast.makeText(getApplicationContext(),"delete done",Toast.LENGTH_SHORT).show();

                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        return view;
    }

    public void startNavigation(Trip trip){

        // delete from array
        // change status in db to done
        Uri uri=Uri.parse("google.navigation:q="+lnglatFromName(trip.getDestination())+"&mode=d");
        Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        intent.setPackage("com.google.android.apps.maps");
        setDurationAndSpeed(trip);
        startActivity(intent);
        TaskManager.getInstance(getActivity()).deleteTask(trip.getId());
    }



    public String lnglatFromName(String tripName){
        return tripName.substring(0,tripName.indexOf("#"));
    }

    public void setDurationAndSpeed(final Trip trip){

        Singleton singleton=Singleton.getInstance(getActivity().getApplicationContext());
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lnglatFromName(trip.getSource()) + "&destination=" + lnglatFromName(trip.getDestination()) + "&key=AIzaSyBP0TBRYhcEWiIJhMM4GyoWWjWovszvGWk";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("success");
                System.out.println(response);
                JSONArray jRoutes;
                JSONArray jLegs;
                long distanceInmeters;
                long durationInSec;
                long avgSpeed;
                try {
                    jRoutes = response.getJSONArray("routes");
                    jLegs = ( (JSONObject)jRoutes.get(0)).getJSONArray("legs");
                    JSONObject jsonObject=jLegs.getJSONObject(0);
                    JSONObject distance=jsonObject.getJSONObject("distance");
                    JSONObject duration=jsonObject.getJSONObject("duration");
                    System.out.println(distance.getString("value")+"<<<<>>>>"+duration.getString("value"));
                    distanceInmeters=distance.getLong("value");
                    durationInSec=duration.getLong("value");
                    System.out.println(distanceInmeters+"<><><><>"+durationInSec);
                    avgSpeed=distanceInmeters/durationInSec;
                    System.out.println(avgSpeed);
                    System.out.println(new DataBaseHandler(getActivity().getApplicationContext()).changeDurationAndSpeed(trip.getId(),durationInSec+"",avgSpeed+""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error   >" + error.getMessage());
                Toast.makeText(getActivity().getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });
        singleton.addToRequestQueue(jsonObjectRequest);
    }


}
