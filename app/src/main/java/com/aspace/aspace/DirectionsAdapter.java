package com.aspace.aspace;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by terrance on 7/19/17.
 */

public class DirectionsAdapter extends BaseAdapter {

    List<NavigationInstruction> instructions;
    Context context;
    int currentStep;

    public DirectionsAdapter(List<NavigationInstruction> instructions, Context context, int currentStep) {
        this.instructions = instructions;
        this.context = context;
        this.currentStep = currentStep;
    }

    @Override
    public int getCount() {
        return instructions.size();
    }

    @Override
    public Object getItem(int position) {
        return instructions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        convertView = LayoutInflater.from(context).inflate(R.layout.direction_list_row, parent, false);
        holder.instructionIconImageView = (ImageView) convertView.findViewById(R.id.instruction_imageview);
        holder.instructionTextView = (TextView) convertView.findViewById(R.id.instruction_label);
        holder.distanceTextView = (TextView) convertView.findViewById(R.id.distance_label);

        NavigationInstruction instruction = instructions.get(position);

        holder.instructionTextView.setText(instruction.getInstruction());
        holder.distanceTextView.setText(instruction.getDistanceFromInstruction());

        if (instruction.getDistanceFromInstruction().isEmpty()) holder.distanceTextView.setVisibility(View.GONE);

        int id = context.getResources().getIdentifier(instruction.getIconFileName(), "drawable", context.getPackageName());
        holder.instructionIconImageView.setImageResource(id);

        if (position == currentStep) {
            holder.instructionTextView.setTypeface(null, Typeface.BOLD);
            holder.distanceTextView.setTypeface(null, Typeface.BOLD);
            holder.distanceTextView.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView instructionIconImageView;
        TextView instructionTextView, distanceTextView;
    }
}
