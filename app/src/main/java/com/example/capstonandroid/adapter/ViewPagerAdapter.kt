import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonandroid.R
import kotlin.collections.ArrayList

class ViewPagerAdapter(idolList: ArrayList<String>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    private var item = idolList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = item.size


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
//        holder.idol.setImageResource()

//        holder.idol.setImageResource(R.drawable.friend)

    val mapImageUrl = item[position]
    println("너는 누구냐" + mapImageUrl)
    Glide.with(holder.itemView.getContext())
        .load(mapImageUrl)
        .override(1500,1500)
        .placeholder(R.drawable.post_picture_loading)
        .error(R.drawable.post_picture_loading)
        .fallback(R.drawable.post_picture_loading)
        .into(holder.idol)

    }

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.idol_list_item, parent, false)){

        val idol = itemView.findViewById<ImageView>(R.id.imageView_idol)!!

    }
}

