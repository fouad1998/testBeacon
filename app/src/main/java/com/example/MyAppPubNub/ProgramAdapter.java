package com.example.MyAppPubNub;



import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProgramAdapter extends ArrayAdapter{
    //to reference the Activity
    private final Activity context;
    private final ArrayList<String[]> beaconList;
    public ProgramAdapter(Activity context, ArrayList<String[]> beaconList){

        super(context,R.layout.beaconlistview_row,beaconList);
        this.context=context;
        this.beaconList = beaconList;


    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.beaconlistview_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView uuid = (TextView) rowView.findViewById(R.id.uuid);
        TextView major = (TextView) rowView.findViewById(R.id.major);
        TextView minor = (TextView) rowView.findViewById(R.id.minor);
        TextView distance = (TextView) rowView.findViewById(R.id.distance);


        //this code sets the values of the objects to values from the arrays
        String[] beacon = beaconList.get(position);
        uuid.setText(beacon[0]);
        major.setText(beacon[1]);
        minor.setText(beacon[2]);
        distance.setText(beacon[3]);



        return rowView;

    };

}

/**public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ViewHolder> {
 private final Activity context;
 private final ArrayList<String[]> beaconList;

 public static  class ViewHolder extends RecyclerView.ViewHolder{
 TextView uuid;
 TextView major;
 TextView minor;
 TextView distance;

 public ViewHolder(@NonNull View itemView) {
 super(itemView);
 //this code gets references to objects in the listview_row.xml file
 uuid = (TextView) itemView.findViewById(R.id.uuid);
 major = (TextView) itemView.findViewById(R.id.major);
 minor = (TextView) itemView.findViewById(R.id.minor);
 distance = (TextView) itemView.findViewById(R.id.distance);
 }
 }

 public ProgramAdapter(Activity context, ArrayList<String[]> beaconList){

 this.context=context;
 this.beaconList = beaconList;


 }

 @NonNull
 @Override
 public ProgramAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
 LayoutInflater inflater = LayoutInflater.from(context);
 View view = inflater.inflate(R.layout.beaconlistview_row, parent,false);
 ViewHolder viewHolder = new ViewHolder(view);
 return null;
 }

 @Override
 public void onBindViewHolder(@NonNull ProgramAdapter.ViewHolder holder, int position) {
 String[] beacon = beaconList.get(position);
 holder.uuid.setText(beacon[0]);
 holder.major.setText(beacon[1]);
 holder.minor.setText(beacon[2]);
 holder.distance.setText(beacon[3]);
 }

 @Override
 public int getItemCount() {
 return beaconList.size();
 }
 /**
 //to reference the Activity
 private final Activity context;
 private final ArrayList beaconList;
 public ProgramAdapter(Activity context, ArrayList beaconList){

 super(context,R.layout.beaconlistview_row);
 this.context=context;
 this.beaconList = beaconList;


 }

 public View getView(int position, View view, ViewGroup parent) {
 LayoutInflater inflater=context.getLayoutInflater();
 View rowView=inflater.inflate(R.layout.beaconlistview_row, null,true);

 //this code gets references to objects in the listview_row.xml file
 TextView uuid = (TextView) rowView.findViewById(R.id.uuid);
 TextView major = (TextView) rowView.findViewById(R.id.major);
 TextView minor = (TextView) rowView.findViewById(R.id.minor);
 TextView distance = (TextView) rowView.findViewById(R.id.distance);

 //this code sets the values of the objects to values from the arrays
 String[] beacon = (String[]) beaconList.get(position);
 uuid.setText(beacon[0]);
 major.setText(beacon[1]);
 minor.setText(beacon[2]);
 distance.setText(beacon[3]);

 return rowView;

 };

 }
 **/