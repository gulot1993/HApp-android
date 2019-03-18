package co.work.fukouka.happ.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.PostDetailsActivity;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;


public class ProfileAdapter extends RecyclerView.Adapter {
    private List<Post> posts = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HappHelper mHelper;

    private final int NO_IMAGE = 0;
    private final int ONE_IMAGE = 1;
    private final int TWO_IMAGE = 2;
    private final int THREE_IMAGE = 3;
    private final int LOADING = 4;
    private int itemPosition;

    private boolean isLoadingAdded = false;

    public ProfileAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHelper = new HappHelper(context);
    }

    private static class NoImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;

        NoImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPostText = (TextView) itemView.findViewById(R.id.tv_post_text);
        }
    }

    private static class OneImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;
        ImageView ivFirstImage;

        OneImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPostText = (TextView) itemView.findViewById(R.id.tv_post_text);
            ivFirstImage = (ImageView) itemView.findViewById(R.id.iv_post_img);
        }
    }

    private static class TwoImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;
        ImageView ivFirstImage;
        ImageView ivSecondImage;

        TwoImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPostText = (TextView) itemView.findViewById(R.id.tv_post_text);
            ivFirstImage = (ImageView) itemView.findViewById(R.id.iv_first_image);
            ivSecondImage = (ImageView) itemView.findViewById(R.id.iv_second_image);
        }
    }

    private static class ThreeImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;
        ImageView ivFirstImage;
        ImageView ivSecondImage;
        ImageView ivThirdImage;

        ThreeImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPostText = (TextView) itemView.findViewById(R.id.tv_post_text);
            ivFirstImage = (ImageView) itemView.findViewById(R.id.iv_first_image);
            ivSecondImage = (ImageView) itemView.findViewById(R.id.iv_second_image);
            ivThirdImage = (ImageView) itemView.findViewById(R.id.iv_third_image);
        }
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        private LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    public void updateList(List<Post> post) {
        for (Iterator<Post> it = post.iterator(); it.hasNext();) {
            Post content = it.next();
            posts.add(content);
            notifyItemInserted(posts.size() - 1);
        }
//        posts.add(post);
//        notifyItemInserted(posts.size() - 1);
    }

    public void add(Post post) {
        posts.add(post);
        notifyItemInserted(posts.size() - 1);
    }

    public void removeItem(int position) {
        posts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, posts.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case NO_IMAGE:
                view = mInflater.inflate(R.layout.layout_no_image, parent, false);

                return new NoImageHolder(view);
            case ONE_IMAGE:
                view = mInflater.inflate(R.layout.layout_one_image_profile, parent, false);

                return new OneImageHolder(view);
            case TWO_IMAGE:
                view = mInflater.inflate(R.layout.layout_two_image, parent, false);

                return new TwoImageHolder(view);
            case THREE_IMAGE:
                view = mInflater.inflate(R.layout.layout_three_image, parent, false);

                return new ThreeImageHolder(view);
            case LOADING:
                view = mInflater.inflate(R.layout.custom_loading_list_item, parent, false);

                return new LoadingHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final int postId = posts.get(position).getPostId();
        String author = posts.get(position).getAuthor();
        final String authorProf = posts.get(position).getAuthorProfile();
        final String date = posts.get(position).getDateModified();
        String body = posts.get(position).getBody();
        final String fromUserId = posts.get(position).getFromUserId();
        final List<String> images = posts.get(position).getImages();
        final String dateMod = date.substring(0, date.length() - 3);
        final String imageUrl1, imageUrl2, imageUrl3;
        String userId = getUserId();

        // Convert html entities to original special characters
        author = mHelper.convertHtmlEntities(author);
        body = mHelper.convertHtmlEntities(body);

        switch (holder.getItemViewType()) {
            case NO_IMAGE:
                NoImageHolder noImageHolder = (NoImageHolder) holder;

                if (fromUserId != null && fromUserId.equals(userId)) {
                    noImageHolder.ivMore.setVisibility(View.VISIBLE);

                    noImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId, position);
                        }
                    });
                } else {
                    noImageHolder.ivMore.setVisibility(View.INVISIBLE);
                }

                mHelper.loadRoundImage(noImageHolder.ivUserThumb, authorProf);
                mHelper.setText(noImageHolder.tvName, author);
                mHelper.setText(noImageHolder.tvDate, dateMod);
                mHelper.setTextGoneIfNull(noImageHolder.tvPostText, body);

                final String finalAuthor6 = author;
                final String finalBody6 = body;
                noImageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostDetailsActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        intent.putExtra("author", finalAuthor6);
                        intent.putExtra("author_profile", authorProf);
                        intent.putExtra("date_mod", dateMod);
                        intent.putExtra("post_content", finalBody6);
                        mContext.startActivity(intent);
                    }
                });

                break;
            case ONE_IMAGE:
                final OneImageHolder oneImageHolder = (OneImageHolder) holder;
                imageUrl1 = images.get(0);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    oneImageHolder.ivMore.setVisibility(View.VISIBLE);

                    oneImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId, position);
                        }
                    });
                } else {
                    oneImageHolder.ivMore.setVisibility(View.INVISIBLE);
                }

                mHelper.loadRoundImage(oneImageHolder.ivUserThumb, authorProf);
                mHelper.setText(oneImageHolder.tvName, author);
                mHelper.setText(oneImageHolder.tvDate, dateMod);
                mHelper.setTextGoneIfNull(oneImageHolder.tvPostText, body);
                mHelper.loadImage(oneImageHolder.ivFirstImage, imageUrl1);

                final String finalBody = body;
                final String finalAuthor = author;
                oneImageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostDetailsActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        intent.putExtra("author", finalAuthor);
                        intent.putExtra("author_profile", authorProf);
                        intent.putExtra("date_mod", dateMod);
                        intent.putExtra("post_content", finalBody);
                        intent.putStringArrayListExtra("images", (ArrayList<String>) images);
                        mContext.startActivity(intent);
                    }
                });

                break;

            case TWO_IMAGE:
                TwoImageHolder twoImageHolder = (TwoImageHolder) holder;

                imageUrl1 = images.get(0);
                imageUrl2 = images.get(1);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    twoImageHolder.ivMore.setVisibility(View.VISIBLE);

                    twoImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId, position);
                        }
                    });
                } else {
                    twoImageHolder.ivMore.setVisibility(View.INVISIBLE);
                }

                mHelper.loadRoundImage(twoImageHolder.ivUserThumb, authorProf);
                mHelper.setText(twoImageHolder.tvName, author);
                mHelper.setText(twoImageHolder.tvDate, dateMod);
                mHelper.setTextGoneIfNull(twoImageHolder.tvPostText, body);
                mHelper.loadImage(twoImageHolder.ivFirstImage, imageUrl1);
                mHelper.loadImage(twoImageHolder.ivSecondImage, imageUrl2);

                final String finalBody1 = body;
                final String finalAuthor1 = author;
                twoImageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostDetailsActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        intent.putExtra("author", finalAuthor1);
                        intent.putExtra("author_profile", authorProf);
                        intent.putExtra("date_mod", dateMod);
                        intent.putExtra("post_content", finalBody1);
                        intent.putStringArrayListExtra("images", (ArrayList<String>) images);
                        mContext.startActivity(intent);
                    }
                });

                break;

            case THREE_IMAGE:
                ThreeImageHolder threeImageHolder = (ThreeImageHolder) holder;

                imageUrl1 = images.get(0);
                imageUrl2 = images.get(1);
                imageUrl3 = images.get(2);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    threeImageHolder.ivMore.setVisibility(View.VISIBLE);

                    threeImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId, position);
                        }
                    });
                } else {
                    threeImageHolder.ivMore.setVisibility(View.INVISIBLE);
                }

                mHelper.loadRoundImage(threeImageHolder.ivUserThumb, authorProf);
                mHelper.setText(threeImageHolder.tvName, author);
                mHelper.setText(threeImageHolder.tvDate, dateMod);
                mHelper.setTextGoneIfNull(threeImageHolder.tvPostText, body);
                mHelper.loadImage(threeImageHolder.ivFirstImage, imageUrl1);
                mHelper.loadImage(threeImageHolder.ivSecondImage, imageUrl2);
                mHelper.loadImage(threeImageHolder.ivThirdImage, imageUrl3);

                final String finalBody3 = body;
                final String finalAuthor5 = author;
                threeImageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostDetailsActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        intent.putExtra("author", finalAuthor5);
                        intent.putExtra("author_profile", authorProf);
                        intent.putExtra("date_mod", dateMod);
                        intent.putExtra("post_content", finalBody3);
                        intent.putStringArrayListExtra("images", (ArrayList<String>) images);
                        mContext.startActivity(intent);
                    }
                });

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int imageCount = posts.get(position).getImages().size();
        int timelineLayout;

        switch (imageCount) {
            case 1:
                timelineLayout = ONE_IMAGE;
                break;
            case 2:
                timelineLayout = TWO_IMAGE;
                break;
            case 3:
                timelineLayout = THREE_IMAGE;
                break;
            default:
                timelineLayout = NO_IMAGE;
        }

        return timelineLayout;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }


    public String getUserId() {
        String userId;

        HashMap<String, String> user = new SessionManager(mContext).getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    private void showOptionDialog(final int postId, final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = mInflater.inflate(R.layout.layout_options_dialog, null);
        builder.setView(view);

        ConstraintLayout clDelete = (ConstraintLayout) view.findViewById(R.id.cl_delete);
        ConstraintLayout clCancel = (ConstraintLayout) view.findViewById(R.id.cl_cancel);
        TextView tvDelete = (TextView) view.findViewById(R.id.tv_delete);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);

        tvDelete.setText(mHelper.delete());
        tvCancel.setText(mHelper.cancel());
        tvTitle.setText(mHelper.managePostDialogTitle());

        final AlertDialog dialog = builder.create();

        clDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePostDialog(postId, position);
                dialog.dismiss();
            }
        });

        clCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        // display dialog
        dialog.show();
    }

    private void deletePostDialog(final int postId, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mContext.getString(R.string.delete_this_post));

        builder.setPositiveButton(mContext.getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost(postId, position);
                    }
                });

        builder.setNegativeButton(mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deletePost(int postId, final int position) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","delete_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("pid", String.valueOf(postId));

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String message = null;
                                if (object != null) {
                                    message = object.getString("mess");
                                }
                                removeItem(position);
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            boolean error = response.getBoolean("error");
                            if (error) {
                                String message = response.getString("message");
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public String getLanguage() {
        String language;

        HashMap<String, String> lang = new SessionManager(mContext).getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }

}
