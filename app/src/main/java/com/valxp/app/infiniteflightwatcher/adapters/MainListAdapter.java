package com.valxp.app.infiniteflightwatcher.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.valxp.app.infiniteflightwatcher.R;
import com.valxp.app.infiniteflightwatcher.model.ATC;
import com.valxp.app.infiniteflightwatcher.model.Fleet;
import com.valxp.app.infiniteflightwatcher.model.Flight;
import com.valxp.app.infiniteflightwatcher.model.Metar;
import com.valxp.app.infiniteflightwatcher.model.Regions;
import com.valxp.app.infiniteflightwatcher.model.Server;
import com.valxp.app.infiniteflightwatcher.model.Users;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ValXp on 6/22/14.
 */
public class MainListAdapter implements ExpandableListAdapter {
    public static final int USERS_INDEX = 0;
    public static final int ATC_INDEX = 1;
    public static final int SERVERS_INDEX = 2;
    private Context mContext;
    private List<Users.User> mUserList;
    private List<ATC> mAtcs;
    private List<Server> mServerList;
    private Fleet mFleet;


    public MainListAdapter(Context context, Fleet fleet, Map<String, List<ATC>> atcs, HashMap<String, Server> servers) {
        mContext = context;
        mFleet = fleet;

        mAtcs = new ArrayList<>();
        if (atcs != null) {
            synchronized (atcs) {
                for (List<ATC> list: atcs.values()) {
                    mAtcs.addAll(list);
                }
                Collections.sort(mAtcs, new Comparator<ATC>() {
                    @Override
                    public int compare(ATC lhs, ATC rhs) {
                        return lhs.name.compareTo(rhs.name);
                    }
                });
            }
        }

        synchronized (fleet) {
            mUserList = new ArrayList<>(fleet.getUsers().getUsers().values());
            for (Iterator<Users.User> it = mUserList.iterator(); it.hasNext();) {
                if (it.next().getCurrentFlight() == null)
                    it.remove();
            }
            Collections.sort(mUserList, new Comparator<Users.User>() {
                @Override
                public int compare(Users.User user, Users.User user2) {
                    Flight flight = user.getCurrentFlight();
                    Flight flight2 = user2.getCurrentFlight();
                    return flight.getUser().getName().compareTo(flight2.getUser().getName());
                }
            });
        }

        mServerList = new ArrayList<>();
        if (servers != null) {
            synchronized (servers) {
                mServerList.addAll(servers.values());
                Collections.sort(mServerList, new Comparator<Server>() {
                    @Override
                    public int compare(Server server, Server server2) {
                        String name;
                        String name2;
                        synchronized (server) {
                            name = server.getName();
                        }
                        synchronized (server2) {
                            name2 = server2.getName();
                        }
                        return name.compareTo(name2);
                    }
                });
            }
        } else {
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getGroupCount() {
        return (mAtcs == null ? 0 : 1) + (mUserList == null ? 0 : 1) + (mServerList == null ? 0 : 1);
    }

    @Override
    public int getChildrenCount(int i) {
        List<Object> list = (List<Object>) getGroup(i);
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getGroup(int i) {
        switch (i) {
            case USERS_INDEX:
                return mUserList;
            case ATC_INDEX:
                return mAtcs;
            case SERVERS_INDEX:
                return mServerList;
            default:
                return null;
        }
    }

    @Override
    public Object getChild(int i, int i2) {
        List<Object> list = (List<Object>) getGroup(i);
        return list != null ? list.get(i2) : null;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.group_view, null);
        }
        TextView name = (TextView) view.findViewById(R.id.group_name);
        TextView count = (TextView) view.findViewById(R.id.group_count);

        count.setText("(" + getChildrenCount(i) + ")");
        switch (i) {
            case USERS_INDEX:
                name.setText("Users");
            break;
            case ATC_INDEX:
                name.setText("ATC");
            break;
            case SERVERS_INDEX:
                name.setText("Servers");
            break;
        }

        return view;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_view, null);
        }
        TextView name = (TextView) view.findViewById(R.id.item_name);
        TextView subname = (TextView) view.findViewById(R.id.item_subname);
        TextView count = (TextView) view.findViewById(R.id.item_count);

        name.setShadowLayer(0, 0, 0, 0);
        subname.setVisibility(View.GONE);
        view.setTag(null);

        Object item = getChild(i, i2);
        int color;
        Regions.Region region;
        if (item == null)
            return view;
        switch (i) {

            case USERS_INDEX:
                Users.User user = (Users.User) item;
                color = android.R.color.black;
                Flight flight = user.getCurrentFlight();
                int bgColor = android.R.color.white;
                if (user.getPilotStats() != null) {
                    if (user.getPilotStats().getGrade() == 3) {
                        color = R.color.grade_3_color;
                    }
                    if (user.getPilotStats().getGrade() == 4) {
                        color = R.color.grade_4_color;
                    }
                }
                if (user.isAdmin()) {
                    color = R.color.dev_color;
                }
                if (user.isMod()) {
                    color = R.color.mod_color;
                }
                name.setShadowLayer(2, 2, 2, mContext.getResources().getColor(bgColor));

                name.setTextColor(mContext.getResources().getColor(color));
                count.setTextColor(mContext.getResources().getColor(android.R.color.black));
                name.setText(user.getName() == null ? "Loading..." : user.getName());
                String text;
                if (flight == null) {
                    text = "Offline";
                    view.setTag(null);
                } else {
                    subname.setVisibility(View.VISIBLE);
                    subname.setText(flight.getLivery().getPlaneName());
                    view.setTag(user.getCurrentFlight());
                }
                count.setText(""); // Used to tell which region it was in.
                break;
            case ATC_INDEX:
                ATC atc = (ATC) item;
                color =  android.R.color.black;
                view.setTag(atc);

                name.setText(atc.user.getName());
                name.setTextColor(mContext.getResources().getColor(color));
                name.setVisibility(View.VISIBLE);

                count.setText(atc.name);
                count.setTextColor(mContext.getResources().getColor(color));
                count.setVisibility(View.VISIBLE);

                subname.setText(atc.type.name());
                subname.setTextColor(mContext.getResources().getColor(color));
                subname.setVisibility(View.VISIBLE);
                break;
            case SERVERS_INDEX:
                Server server = (Server) item;
                synchronized (server) {
                    view.setTag(server);
                    color = mFleet.getSelectedServer() == server ? R.color.orange_color : android.R.color.black;
                    name.setTextColor(mContext.getResources().getColor(color));
                    name.setText(server.getName());
                    count.setTextColor(mContext.getResources().getColor(color));
                    count.setText(server.getUserCount() + "/" + server.getMaxUsers());
                }
                break;
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int i) {

    }

    @Override
    public void onGroupCollapsed(int i) {

    }

    @Override
    public long getCombinedChildId(long l, long l2) {
        return getCombinedGroupId(l) + l2;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return l * 1000;
    }
}
