import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.Data.ItemDescription
import com.example.motherload.R
import com.squareup.picasso.Picasso

class InventoryAdapter(private val itemList: List<ItemDescription>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(item: ItemDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val rarityTextView: TextView = itemView.findViewById(R.id.rarity)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)

        fun bind(item: ItemDescription) {
            Log.d("item", item.image)
            Picasso.get().load(item.image).into(imageView)
            nameTextView.text = item.nom
            rarityTextView.text = item.rarity
            quantityTextView.text = item.quantity

            itemView.setOnClickListener {
                itemClickListener.onItemClick(item)
            }
        }
    }
}