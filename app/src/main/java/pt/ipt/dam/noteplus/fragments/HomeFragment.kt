package pt.ipt.dam.noteplus.fragments
import pt.ipt.dam.noteplus.adapter.NoteAdapter
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import pt.ipt.dam.noteplus.R

@Suppress("DEPRECATION")
class HomeFragment() : Fragment(R.layout.home_fragment), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // Adicione esta linha para habilitar o menu
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu) // Infle o menu
        val searchItem = menu.findItem(R.id.searchMenu)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Lógica para pesquisar as notas
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Lógica para atualizar a pesquisa em tempo real
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchMenu -> {
                // Ação para encontrar as notas
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeFragment> {
        override fun createFromParcel(parcel: Parcel): HomeFragment {
            return HomeFragment(parcel)
        }

        override fun newArray(size: Int): Array<HomeFragment?> {
            return arrayOfNulls(size)
        }
    }
}