package co.work.fukouka.happ.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.PostDetailsActivity;
import co.work.fukouka.happ.activity.ProfileActivity;
import co.work.fukouka.happ.helper.AlertDialogHelper;
import co.work.fukouka.happ.helper.EllipsizingTextView;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.presenter.PostPresenter;
import co.work.fukouka.happ.presenter.ProfilePresenter;
import co.work.fukouka.happ.view.ManagePostView;
import co.work.fukouka.happ.view.PostView;

public class TimelineAdapter extends RecyclerView.Adapter implements PostView, ManagePostView {

    private List<Post> posts = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private PostPresenter mPresenter;
    private adapterListener mListener;
    private HappHelper mHelper;
    private Activity mActivity;
    private ProfilePresenter mProfPres;

    private int itemPosition;
    private final int NO_IMAGE = 0;
    private final int ONE_IMAGE = 1;
    private final int TWO_IMAGE = 2;
    private final int THREE_IMAGE = 3;

    private boolean notFirstLoad;

    private static class NoImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;

        NoImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = itemView.findViewById(R.id.iv_user_thumb);
            ivMore =  itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPostText = itemView.findViewById(R.id.tv_post_text);
        }
    }

    private static class OneImageHolder extends RecyclerView.ViewHolder{
        ImageView ivUserThumb;
        ImageView ivMore;
        TextView tvName;
        TextView tvDate;
        TextView tvPostText;
        ImageView ivFirstImage;
        ProgressBar progressBar;

        OneImageHolder(View itemView) {
            super(itemView);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPostText = (TextView) itemView.findViewById(R.id.tv_post_text);
            ivFirstImage = (ImageView) itemView.findViewById(R.id.iv_post_img);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }

    private static class TwoImageHolder extends RecyclerView.ViewHolder {
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

    public interface adapterListener {
        void updateTimeline();
        void refreshTimeline();
    }

    public TimelineAdapter(Context context, adapterListener listener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mPresenter = new PostPresenter(context, this);
        mProfPres = new ProfilePresenter(context, this);
        mHelper = new HappHelper(context);
        this.mListener = listener;
        mActivity = (Activity) context;
    }

    public void addPost(List<Post> post, String action) {
        if (action.equals("refresh")) {
            posts = post;
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < post.size(); i++) {
                posts.add(post.get(i));
                notifyItemInserted(posts.size() - 1);
            }
        }
    }

    public void appendNewPost(Post post) {
        posts.add(0, post);
        notifyDataSetChanged();
    }

    public void appendAuthorNewPost(Post post) {
        posts.add(0, post);
        notifyItemInserted(0);
        mListener.updateTimeline();
    }

    public void appendNewPost(List<Post> post) {
        for (Post content: post) {
            posts.add(0, content);
            notifyDataSetChanged();
        }
    }

    public void appendAuthorNewPost(List<Post> post) {
        for (Post content: post) {
            posts.add(0, content);
            notifyDataSetChanged();
            mListener.updateTimeline();
        }
    }

    public void clearList() {
        int size = this.posts.size();
        this.posts.clear();
        notifyItemRangeRemoved(0, size);
    }

    private void removeItem(int position) {
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
                view = mInflater.inflate(R.layout.layout_one_image, parent, false);

                return new OneImageHolder(view);
            case TWO_IMAGE:
                view = mInflater.inflate(R.layout.layout_two_image, parent, false);

                return new TwoImageHolder(view);
            case THREE_IMAGE:
                view = mInflater.inflate(R.layout.layout_three_image, parent, false);

                return new ThreeImageHolder(view);
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
        final String userId = mPresenter.getUserId();

        final String dateMod = date.substring(0, date.length() - 3);
        final String imageUrl1, imageUrl2, imageUrl3;

        // Convert html entities to original special characters
        author = mHelper.convertHtmlEntities(author);
        body = mHelper.convertHtmlEntities(body);

        switch (holder.getItemViewType()) {
            case NO_IMAGE:
                final NoImageHolder noImageHolder = (NoImageHolder) holder;
                //noImageHolder.tvPostText.setMaxLines(3);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    noImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId);
                        }
                    });
                } else {
                    noImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showMoreOptionDialog(postId, fromUserId);
                        }
                    });
                }

                mHelper.loadRoundImage(noImageHolder.ivUserThumb, authorProf);
                mHelper.setText(noImageHolder.tvName, author);
                mHelper.setText(noImageHolder.tvDate, dateMod);
                //mHelper.makeTextViewResizable(noImageHolder.tvPostText, body, 7, "Continue Reading");
                mHelper.setTextGoneIfNull(noImageHolder.tvPostText, body);

                noImageHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
                noImageHolder.tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
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
                        mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                });

                break;
            case ONE_IMAGE:
                final OneImageHolder oneImageHolder = (OneImageHolder) holder;
                imageUrl1 = images.get(0);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    oneImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId);
                        }
                    });
                } else {
                    oneImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showMoreOptionDialog(postId, fromUserId);
                        }
                    });
                }

                mHelper.loadRoundImage(oneImageHolder.ivUserThumb, authorProf);
                mHelper.setText(oneImageHolder.tvName, author);
                mHelper.setText(oneImageHolder.tvDate, dateMod);
                //mHelper.makeTextViewResizable(oneImageHolder.tvPostText, body, 7, "Continue Reading");
                mHelper.setTextGoneIfNull(oneImageHolder.tvPostText, body);
                mHelper.loadImageWithProgressBar(oneImageHolder.ivFirstImage, imageUrl1, oneImageHolder.progressBar);

                //click listeners
                oneImageHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
                oneImageHolder.tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });

                final String finalBody = body;
                final String finalAuthor = author;
                oneImageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //mListener.onImageClick(imageUrl1);
                        Intent intent = new Intent(mContext, PostDetailsActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        intent.putExtra("author", finalAuthor);
                        intent.putExtra("author_profile", authorProf);
                        intent.putExtra("date_mod", dateMod);
                        intent.putExtra("post_content", finalBody);
                        intent.putStringArrayListExtra("images", (ArrayList<String>) images);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                });

                break;

            case TWO_IMAGE:
                TwoImageHolder twoImageHolder = (TwoImageHolder) holder;

                imageUrl1 = images.get(0);
                imageUrl2 = images.get(1);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    twoImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId);
                        }
                    });
                } else {
                    twoImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showMoreOptionDialog(postId, fromUserId);
                        }
                    });
                }

                mHelper.loadRoundImage(twoImageHolder.ivUserThumb, authorProf);
                mHelper.setText(twoImageHolder.tvName, author);
                mHelper.setText(twoImageHolder.tvDate, dateMod);
               // mHelper.makeTextViewResizable(twoImageHolder.tvPostText, body, 7, "Continue Reading");
                mHelper.setTextGoneIfNull(twoImageHolder.tvPostText, body);
                mHelper.loadImage(twoImageHolder.ivFirstImage, imageUrl1);
                mHelper.loadImage(twoImageHolder.ivSecondImage, imageUrl2);

                twoImageHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
                twoImageHolder.tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });

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
                        mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                });

                break;

            case THREE_IMAGE:
                ThreeImageHolder threeImageHolder = (ThreeImageHolder) holder;

                imageUrl1 = images.get(0);
                imageUrl2 = images.get(1);
                imageUrl3 = images.get(2);

                if (fromUserId != null && fromUserId.equals(userId)) {
                    threeImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showOptionDialog(postId);
                        }
                    });
                } else {
                    threeImageHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            itemPosition = position;
                            showMoreOptionDialog(postId, fromUserId);
                        }
                    });
                }

                mHelper.loadRoundImage(threeImageHolder.ivUserThumb, authorProf);
                mHelper.setText(threeImageHolder.tvName, author);
                mHelper.setText(threeImageHolder.tvDate, dateMod);
               // mHelper.makeTextViewResizable(threeImageHolder.tvPostText, body, 7, "Continue Reading");
                mHelper.setTextGoneIfNull(threeImageHolder.tvPostText, body);
                mHelper.loadImage(threeImageHolder.ivFirstImage, imageUrl1);
                mHelper.loadImage(threeImageHolder.ivSecondImage, imageUrl2);
                mHelper.loadImage(threeImageHolder.ivThirdImage, imageUrl3);

                threeImageHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
                threeImageHolder.tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("user_id", fromUserId);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    }
                });
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
                        mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

    @Override
    public void onLoadPosts(List<Post> post, String action) {}

    @Override
    public void appendAuthorPost(List<Post> post) {

    }

    /**
     * Posts successfully deleted
     */
    @Override
    public void onSuccess(String message) {
        removeItem(itemPosition);
        //store id of new latest post
        mPresenter.getLatestPost();
    }

    /**
     * Failed upon post deletion
     */
    @Override
    public void onFailed(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoNewPost() {}

    @Override
    public void onFreetimeFailed() {}

    @Override
    public void isFree(boolean isFree) {}

    private void showOptionDialog(final int postId){
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
                deletePostDialog(postId);
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

    private void showMoreOptionDialog(final int postId, final String fromUserId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = mInflater.inflate(R.layout.layout_more_options, null);
        builder.setView(view);

        ConstraintLayout clBlock = (ConstraintLayout) view.findViewById(R.id.cl_block);
        ConstraintLayout clReport = (ConstraintLayout) view.findViewById(R.id.cl_report);
        ConstraintLayout clCancel = (ConstraintLayout) view.findViewById(R.id.cl_cancel);
        TextView tvBlock = (TextView) view.findViewById(R.id.tv_block);
        TextView tvReport = (TextView) view.findViewById(R.id.tv_report);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);

        tvBlock.setText(mHelper.blockUser());
        tvReport.setText(mHelper.reportPost());
        tvCancel.setText(mHelper.cancel());
        tvTitle.setText(mHelper.managePostDialogTitle());

        final AlertDialog dialog = builder.create();

        clBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfPres.getMessageThread(Integer.parseInt(fromUserId));
                blockUserDialog(fromUserId);
                dialog.dismiss();
            }
        });

        clReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUserDialog(postId, fromUserId);
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

    private void deletePostDialog(final int postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mHelper.deletePost());

        builder.setPositiveButton(mHelper.delete(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.deletePost(postId);
                    }
                });

        builder.setNegativeButton(mHelper.cancel(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void blockUserDialog(final String userId) {
        AlertDialogHelper.showAlert(mContext, mHelper.blockUser(), mHelper.block(), mHelper.cancel(), new AlertDialogHelper.Callback() {
            @Override
            public void onPositiveButtonClick() {
                mProfPres.blockUserFromTimeline(userId);
            }
        });
    }

    private void reportUserDialog(final int postId, final String userId) {
        AlertDialogHelper.showAlert(mContext, mHelper.reportPost(), mHelper.report(), mHelper.cancel(), new AlertDialogHelper.Callback() {
            @Override
            public void onPositiveButtonClick() {
                mProfPres.reportUser(userId, String.valueOf(postId));
            }
        });
    }

    @Override
    public void onUserBlocked(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        mListener.refreshTimeline();
    }

    @Override
    public void onUserReported(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
