package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.social.SocialLinksFragment
import org.fossasia.openevent.general.ticket.TicketsFragment
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class EventDetailsFragment : Fragment() {
    val EVENT_ID = "EVENT_ID"
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private lateinit var rootView: View
    private var eventId: Long = -1
    private lateinit var eventShare: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        eventViewModel.event.observe(this, Observer {
            it?.let {
                loadEvent(it)
                eventShare = it
            }
            loadTicketFragment()
            loadSocialLinksFragment()
            Timber.d("Fetched events of id %d", eventId)
        })

        eventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventViewModel.loadEvent(eventId)

        return rootView
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.eventName.text = event.name
        rootView.eventOrganiserName.text = "by " + event.organizerName.nullToEmpty()
        setTextField(rootView.eventDescription, event.description)
        setTextField(rootView.eventOrganiserDescription, event.organizerDescription)
        rootView.eventLocationTextView.text = event.locationName

        rootView.startsOn.text = "${startsAt.dayOfMonth} ${startsAt.month} ${startsAt.year}"
        rootView.endsOn.text = "${endsAt.dayOfMonth} ${endsAt.month} ${endsAt.year}"

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(rootView.logo)
        }

        //load location to map
        rootView.locationUnderMap.text = event.locationName
        val mapClickListener = View.OnClickListener { startMap(event) }
        if (event.locationName != null) {
            rootView.eventLocationLinearLayout.visibility = View.VISIBLE
            rootView.locationUnderMap.visibility = View.VISIBLE
            rootView.imageMap.visibility = View.VISIBLE
            rootView.imageMap.setOnClickListener(mapClickListener)
            rootView.eventLocationTextView.setOnClickListener(mapClickListener)
        }

        Picasso.get()
                .load(eventViewModel.loadMap(event))
                .placeholder(R.drawable.ic_map_black_24dp)
                .into(rootView.imageMap)

        //Add event to Calendar
        val dateClickListener = View.OnClickListener { startCalendar(event) }
        rootView.startsOn.setOnClickListener(dateClickListener)
        rootView.endsOn.setOnClickListener(dateClickListener)

    }

    override fun onDestroyView() {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.add_to_calendar -> {
                //Add event to Calendar
                startCalendar(eventShare)
                return true
            }
            R.id.event_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(eventShare))
                sendIntent.type = "text/plain"
                rootView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        if (value.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = value
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.setGroupVisible(R.id.event_menu, true)
        super.onPrepareOptionsMenu(menu)
    }

    private fun startCalendar(event: Event){
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, EventUtils.getTimeInMilliSeconds(event.startsAt))
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, EventUtils.getTimeInMilliSeconds(event.endsAt))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val inflaterMenu = activity?.menuInflater
        inflaterMenu?.inflate(R.menu.event_details, menu)
    }
  
    private fun loadTicketFragment(){
        //Initialise Ticket Fragment
        val ticketFragment = TicketsFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        ticketFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.frameContainer, ticketFragment).commit()
    }

    private fun loadSocialLinksFragment(){
        //Initialise SocialLinks Fragment
        val socialLinksFragemnt = SocialLinksFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        socialLinksFragemnt.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.frameContainerSocial, socialLinksFragemnt).commit()
    }
  
    private fun startMap(event: Event) {
        // start map intent
        val mapUrl = eventViewModel.loadMapUrl(event)
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        if (mapIntent.resolveActivity(activity?.packageManager) != null) {
            startActivity(mapIntent)
        }
    }
}