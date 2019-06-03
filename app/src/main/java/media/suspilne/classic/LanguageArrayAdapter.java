package media.suspilne.classic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LanguageArrayAdapter extends ArrayAdapter<Country> {
    private int groupid;
    private ArrayList<Country> list;
    private LayoutInflater inflater;

    public LanguageArrayAdapter(Activity context, int groupid, ArrayList<Country> list){
        super(context,0, list);
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groupid = groupid;
    }

    public View getView(int position, View convertView, ViewGroup parent ){
        View itemView = inflater.inflate(groupid, parent, false);

        ((ImageView)itemView.findViewById(R.id.flag)).setImageResource(list.get(position).flag);
        ((TextView)itemView.findViewById(R.id.code)).setText(list.get(position).code);
        ((TextView)itemView.findViewById(R.id.title)).setText(list.get(position).title);

        return itemView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent){
        return getView(position,convertView,parent);
    }
}

class Country{
    public String code;
    public String title;
    public int flag;

    public Country(String code, String title, int flag){
        this.code = code;
        this.title = title;
        this.flag = flag;
    }
}
