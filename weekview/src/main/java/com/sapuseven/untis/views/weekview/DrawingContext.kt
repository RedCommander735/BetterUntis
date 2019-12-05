package com.sapuseven.untis.views.weekview

import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import org.joda.time.DateTime
import kotlin.math.ceil

class DrawingContext(val startPixel: Float) {
	var dayRange: List<DateTime> = emptyList()
	var freeDays: List<Pair<DateTime, Float>> = emptyList()

	companion object {
		internal fun create(config: WeekViewConfig, viewState: WeekViewViewState): DrawingContext {
			val today = DateTime.now().withTimeAtStartOfDay()
			val daysScrolled = (ceil((config.drawConfig.currentOrigin.x / config.totalDayWidth).toDouble()) * -1).toInt()
			val startPixel = (config.drawConfig.currentOrigin.x
					+ config.totalDayWidth * daysScrolled
					+ config.drawConfig.timeColumnWidth)

			val dayRange = mutableListOf<DateTime>()
			if (config.isSingleDay) {
				viewState.firstVisibleDay?.let { dayRange.add(it) }
			} else {
				val offset = if (config.snapToWeek) DateUtils.offsetInWeek(today, config.firstDayOfWeek) else 0
				val offsetCompensation = DateUtils.offsetInWeek(today, config.firstDayOfWeek) - offset // This is to start calculations at the first day of week while leaving the visible offset at the correct location

				dayRange.addAll(DateUtils.getDateRange(today.plusDays(DateUtils.actualDays(daysScrolled + offsetCompensation, config.weekLength) - offset - offsetCompensation), config.visibleDays + 1, config.firstDayOfWeek, config.weekLength))
			}

			return DrawingContext(startPixel).apply { this.dayRange = dayRange }
		}
	}
}
