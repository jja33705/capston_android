import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.capstonandroid.R
import com.example.capstonandroid.network.dto.ListViewItem
import com.example.capstonandroid.network.dto.ListViewItem2
import kotlinx.android.synthetic.main.custom_list_item.view.*

class ListViewAdapter2(private val items2: MutableList<ListViewItem2>): BaseAdapter()
{ override fun getCount(): Int = items2.size
    override fun getItem(position: Int): ListViewItem2 = items2[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View { var convertView = view
        if (convertView == null) convertView = LayoutInflater.from(parent?.context).inflate(R.layout.custom_list_item,
            parent, false)
        val item2: ListViewItem2 = items2[position]
        convertView!!.image_title.setImageDrawable(item2.icon)
        convertView.text_title.text = item2.title
        convertView.text_date.text = item2.subTitle
                return convertView } }
