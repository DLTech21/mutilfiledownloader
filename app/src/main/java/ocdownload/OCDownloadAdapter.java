package ocdownload;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.dl.filedownloader.R;

import java.util.ArrayList;

public class OCDownloadAdapter extends RecyclerView.Adapter{

    private ArrayList<DownloadInfo> downloadInfoArrayList;
//    private DownloadCompparator compparator;
    private OnRecycleViewClickCallBack recycleViewClickCallBack;

    public OCDownloadAdapter(ArrayList<DownloadInfo> data) {
        this.downloadInfoArrayList = data;
//        this.compparator = new DownloadCompparator();
    }


    /**
     * 获取某项在列表中的位置
     * @param bean  对象
     * @return  在Adapter中的位置
     */
    public int getItemPosition(DownloadInfo bean){
        return downloadInfoArrayList.indexOf(bean);
    }

    /**
     * 设置点击 长按监听
     * @param recycleViewClickCallBack  监听接口
     */
    public void setRecycleViewClickCallBack(OnRecycleViewClickCallBack recycleViewClickCallBack){
        this.recycleViewClickCallBack = recycleViewClickCallBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder)holder;
        DownloadInfo bean = downloadInfoArrayList.get(position);
        viewHolder.title.setText(downloadInfoArrayList.get(position).getTitle());
        viewHolder.textProgress.setText(downloadInfoArrayList.get(position).getTextProgress());
        viewHolder.progressBar.setProgress(downloadInfoArrayList.get(position).getProgress());
        switch (bean.getStatus()){
            case DownloadConfig.DOWNLOADING:
                viewHolder.status.setText("Downloading");
                break;
            case DownloadConfig.WAITTING:
                viewHolder.status.setText("Waitting");
                break;
            case DownloadConfig.FAILED:
                viewHolder.status.setText("Failed");
                break;
            case DownloadConfig.PAUSED:
                viewHolder.status.setText("Paused");
                break;
            case DownloadConfig.FINISH:
                viewHolder.status.setText("Finish");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return downloadInfoArrayList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        TextView title;
        TextView textProgress;
        TextView status;
        ProgressBar progressBar;

        public ItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.textView_download_title);
            status = (TextView)itemView.findViewById(R.id.textView_download_status);
            textProgress = (TextView)itemView.findViewById(R.id.textView_download_length);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar_download);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if (recycleViewClickCallBack != null){
                recycleViewClickCallBack.onRecycleViewLongClick(downloadInfoArrayList.get(getAdapterPosition()));
            }
            return true;
        }

        @Override
        public void onClick(View v) {
            if (recycleViewClickCallBack != null){
                int index = getAdapterPosition();
                recycleViewClickCallBack.onRecycleViewClick(downloadInfoArrayList.get(index));
            }
        }
    }

//    /**
//     * 下载列表排序器
//     */
//    class DownloadCompparator implements Comparator<DownloadInfo>{
//        @Override
//        public int compare(DownloadInfo lhs, DownloadInfo rhs) {
//            return rhs.getStatus().ordinal() - lhs.getStatus().ordinal();
//        }
//
//        @Override
//        public boolean equals(Object object) {
//            return object instanceof DownloadInfo;
//        }
//    }

    interface OnRecycleViewClickCallBack{

        void onRecycleViewClick(DownloadInfo bean);

        void onRecycleViewLongClick(DownloadInfo bean);

    }

}
