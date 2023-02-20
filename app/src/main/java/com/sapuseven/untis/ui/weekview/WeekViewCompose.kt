package com.sapuseven.untis.ui.weekview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToInt

data class Event(
	val name: String,
	val color: Color,
	val start: LocalDateTime,
	val end: LocalDateTime,
	val description: String? = null,
)

val eventTimeFormat = DateTimeFormat.forPattern("h:mm a")

@Composable
fun WeekViewEvent(
	event: Event,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(end = 2.dp, bottom = 2.dp)
			.background(event.color, shape = RoundedCornerShape(4.dp))
			.padding(4.dp)
	) {
		Text(
			text = "${eventTimeFormat.print(event.start)} - ${eventTimeFormat.print(event.end)}"
		)

		Text(
			text = event.name,
			fontWeight = FontWeight.Bold,
		)

		if (event.description != null) {
			Text(
				text = event.description,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}


@Preview(showBackground = true)
@Composable
fun EventPreview() {
	WeekViewEvent(
		event = Event(
			name = "Test event",
			color = Color(0xFFAFBBF2),
			start = LocalDateTime.parse("2021-05-18T09:00:00"),
			end = LocalDateTime.parse("2021-05-18T11:00:00"),
			description = "This is an example event.",
		), modifier = Modifier.sizeIn(maxHeight = 64.dp)
	)
}

private class EventDataModifier(
	val event: Event,
) : ParentDataModifier {
	override fun Density.modifyParentData(parentData: Any?) = event
}

private fun Modifier.eventData(event: Event) = this.then(EventDataModifier(event))

private val dayNameFormat = DateTimeFormat.forPattern("EEE")
private val dayDateFormat = DateTimeFormat.forPattern("d. MMM")

@Composable
fun WeekViewHeaderDay(
	day: LocalDate,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = Modifier
			.padding(8.dp)
	) {
		Text(
			text = dayNameFormat.print(day),
			textAlign = TextAlign.Center,
			fontSize = 20.sp,
			fontWeight = FontWeight.Medium,
			modifier = modifier
				.fillMaxWidth()
		)
		Text(
			text = dayDateFormat.print(day),
			textAlign = TextAlign.Center,
			fontSize = 14.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = modifier
				.fillMaxWidth()
		)
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewHeaderDayPreview() {
	WeekViewHeaderDay(day = LocalDate.now())
}

@Composable
fun WeekViewHeader(
	startDate: LocalDate,
	numDays: Int,
	modifier: Modifier = Modifier,
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it) },
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
	) {
		repeat(numDays) { i ->
			Box(modifier = Modifier.weight(1f)) {
				dayHeader(startDate.plusDays(i))
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewHeaderPreview() {
	WeekViewHeader(
		startDate = LocalDate.now(),
		numDays = 5
	)
}

private val HourFormatter = DateTimeFormat.forPattern("hh")

@Composable
fun WeekViewSidebarLabel(
	time: LocalTime,
	modifier: Modifier = Modifier,
) {
	Text(
		text = HourFormatter.print(time),
		modifier = modifier
			.fillMaxHeight()
			.padding(4.dp)
	)
}

@Preview(showBackground = true)
@Composable
fun BasicSidebarLabelPreview() {
	WeekViewSidebarLabel(time = LocalTime.MIDNIGHT, Modifier.sizeIn(maxHeight = 64.dp))
}

@Composable
fun WeekViewSidebar(
	hourHeight: Dp,
	modifier: Modifier = Modifier,
	label: @Composable (time: LocalTime) -> Unit = { WeekViewSidebarLabel(time = it) },
) {
	Column(modifier = modifier) {
		val startTime = LocalTime.MIDNIGHT
		repeat(24) { i ->
			Box(modifier = Modifier.height(hourHeight)) {
				label(startTime.plusHours(i))
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewSidebarPreview() {
	WeekViewSidebar(hourHeight = 64.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekViewCompose(
	events: List<Event> = emptyList(),
	modifier: Modifier = Modifier,
	eventContent: @Composable (event: Event) -> Unit = { WeekViewEvent(event = it) },
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it) },
	startDate: LocalDate = LocalDate.now(),
) {
	val hourHeight = 64.dp
	val verticalScrollState = rememberScrollState()
	var sidebarWidth by remember { mutableStateOf(0) }
	var headerHeight by remember { mutableStateOf(0) }


	val yourList = (1..5).map { it.toString() }
	val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)

	val positionFromIWantToStart = 3
	val itemsCount = yourList.size

	val numPages = Int.MAX_VALUE / itemsCount
	val startPage = numPages / 2
	val startIndex = (startPage * itemsCount) + positionFromIWantToStart


	val pagerState = rememberPagerState(initialPage = startIndex)

	Row(modifier = modifier) {
		WeekViewSidebar(
			hourHeight = hourHeight,
			modifier = Modifier
				.padding(top = with(LocalDensity.current) { headerHeight.toDp() })
				.onGloballyPositioned { sidebarWidth = it.size.width }
				.verticalScroll(verticalScrollState)
		)

		HorizontalPager(
			state = pagerState,
			pageCount = Int.MAX_VALUE,
		) { index ->
			Column {
				WeekViewHeader(
					startDate = startDate,
					numDays = 5,
					dayHeader = dayHeader,
					modifier = Modifier
						.onGloballyPositioned { headerHeight = it.size.height }
				)

				val page = index % itemsCount

				WeekViewContent(
					events = events,
					eventContent = eventContent,
					startDate = startDate,
					numDays = 5,
					hourHeight = hourHeight,
					modifier = Modifier
						.weight(1f)
						.verticalScroll(verticalScrollState)
				)
			}
		}
	}
}

@Composable
fun WeekViewContent(
	events: List<Event>,
	modifier: Modifier = Modifier,
	eventContent: @Composable (event: Event) -> Unit = { WeekViewEvent(event = it) },
	startDate: LocalDate,
	numDays: Int = 5,
	hourHeight: Dp,
) {
	val dividerColor = MaterialTheme.colorScheme.outline
	var dayWidth by remember { mutableStateOf(0f) }

	Layout(
		content = {
			events.sortedBy(Event::start).forEach { event ->
				Box(modifier = Modifier.eventData(event)) {
					eventContent(event)
				}
			}
		},
		modifier = modifier
			.onGloballyPositioned { dayWidth = it.size.width / numDays.toFloat() }
			.drawBehind {
				repeat(23) {
					drawLine(
						dividerColor,
						start = Offset(0f, (it + 1) * hourHeight.toPx()),
						end = Offset(size.width, (it + 1) * hourHeight.toPx()),
						strokeWidth = 1.dp.toPx()
					)
				}
				repeat(numDays - 1) {
					drawLine(
						dividerColor,
						start = Offset((it + 1) * dayWidth, 0f),
						end = Offset((it + 1) * dayWidth, size.height),
						strokeWidth = 1.dp.toPx()
					)
				}
			}
	) { measureables, constraints ->
		val height = hourHeight.roundToPx() * 24
		val width = constraints.maxWidth
		val placeablesWithEvents = measureables.map { measurable ->
			val event = measurable.parentData as Event
			val eventDurationMinutes = Minutes.minutesBetween(event.start, event.end).minutes
			val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
			val placeable = measurable.measure(
				constraints.copy(
					minHeight = eventHeight,
					maxHeight = eventHeight
				)
			)
			Pair(placeable, event)
		}
		layout(width, height) {
			placeablesWithEvents.forEach { (placeable, event) ->
				val eventOffsetMinutes =
					Minutes.minutesBetween(LocalTime.MIDNIGHT, event.start.toLocalTime()).minutes
				val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
				val eventOffsetDays =
					Minutes.minutesBetween(startDate, event.start.toLocalDate()).minutes
				val eventX = eventOffsetDays * (constraints.maxWidth / numDays)
				placeable.place(eventX, eventY)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewPreview() {
	WeekViewCompose()
}