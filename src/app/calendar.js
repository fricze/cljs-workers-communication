import ReactDOM, { render, } from "react-dom"
import h from "react-hyperscript"
import React, { useState, } from "react"

export function getDaysInMonthRecursive(
  month,
  year,
  date = new Date(year, month, 1),
  days = []
) {
  if (date.getMonth() === month) {
    const nextDate = new Date(date)
    nextDate.setDate(date.getDate() + 1)

    return getDaysInMonthRecursive(
      month,
      year,
      nextDate,
      days.concat([new Date(date),])
    )
  }

  return days
}

function getDaysInMonth(month, year) {
  const date = new Date(year, month, 1)
  const days = []

  while (date.getMonth() === month) {
    days.push(new Date(date))
    date.setDate(date.getDate() + 1)
  }

  return days
}

const weekDays = [
  "Sunday",
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
].map(d => d.slice(0, 3))

const element = h(
  "div",
  { className: "weekdays", key: "weekdays", },
  weekDays.map(name => h("div", { key: name, className: "weekday", }, name))
)

const useValue = handler => e => handler(e.target.value)

const useCalendarState = () => {
  const [[month, year,], setMonthYear,] = useState([4, 2027,])

  // System boundaries. Allow only numbers between 0 and 11
  const setMonth = value => {
    const monthNumber = Number(value)
    const newMonthValue =
      monthNumber > 11 ? 11 : monthNumber < 0 ? 0 : monthNumber

    return setMonthYear([newMonthValue, year,])
  }
  // System boundaries. Set year number
  const setYear = value => {
    const yearNumber = Number(value)
    const newYearValue = yearNumber < 1990 ? 1990 : yearNumber

    return setMonthYear([month, newYearValue,])
  }

  return {
    setYear,
    setMonth,
    month,
    year,
  }
}

const getNextDays = (month, year) =>
  month === 11
    ? getDaysInMonthRecursive(0, year + 1)
    : getDaysInMonthRecursive(month + 1, year)

const getPrevDays = (month, year) =>
  month === 0
    ? getDaysInMonthRecursive(11, year - 1)
    : getDaysInMonthRecursive(month - 1, year)

const getCalendarBlock = ({ month, year, daysPerRow, }) => {
  const daysCollection = getDaysInMonthRecursive(month, year)

  // System boundaries. Make sure to go correctly to next month in following year
  const nextDaysCollection = getNextDays(month, year)
  // System boundaries. Make sure to go correctly to previus month in previous year
  const prevDaysCollection = getPrevDays(month, year)

  const firstDay = daysCollection[0]
  const howFarToLeft = firstDay.getDay() % daysPerRow

  const lastFromPrevDays = prevDaysCollection.slice(
    Math.max(prevDaysCollection.length - howFarToLeft, 1)
  )
  const currentWithPrev = lastFromPrevDays.concat(daysCollection)

  const rows = Math.ceil(currentWithPrev.length / daysPerRow)
  const shouldHaveCells = rows * daysPerRow
  const howFarToRight = shouldHaveCells - currentWithPrev.length

  const firstFromNextDays = nextDaysCollection.slice(0, howFarToRight)

  return currentWithPrev.concat(firstFromNextDays)
}

export const Calendar = ({ daysPerRow = 7, _getCalendarBlock = getCalendarBlock, }) => {
  const { setYear, setMonth, month, year, } = useCalendarState()

  const allDaysCollection = _getCalendarBlock({ month, year, daysPerRow, })

  return h(
    "div",
    { className: "box", },
    h("div", [
      element,

      h(
        "div",
        {
          className: "grid",
          key: "grid",
          style: {
            gridTemplateColumns: `repeat(${daysPerRow}, 40px)`,
          },
        },
        allDaysCollection.map((x, idx) => {
          const column = (idx % daysPerRow) + 1

          return h(
            "span",
            {
              // this key should be unique enough,
              // timestamp seems to be good identifier for Date
              key: x.getTime(),
              style: {
                gridColumn: column,
              },
              className: "day",
            },
            x.getDate()
          )
        })
      ),

      h("input", {
        key: "month-input",
        type: "number",
        value: month,
        // System boundaries.
        onChange: useValue(setMonth),
      }),

      h("input", {
        key: "year-input",
        type: "number",
        value: year,
        // System boundaries.
        onChange: useValue(setYear),
      }),
    ])
  )
}

// const root = h(Calendar)

// render(root, document.getElementById("root"))
