package com.kotoryx.Rebukkit

import org.bukkit.ChatColor
import java.util.*

class RebukkitUtil
{
    companion object
    {
        fun color(arg0 : String) : String = ChatColor.translateAlternateColorCodes('&', arg0)

        fun colorList(l: MutableList<String>): MutableList<String>
        {
            for (i in l.indices) {
                val s = l[i]
                val reg = "&f" + s
                l[i] = ChatColor.translateAlternateColorCodes('&', reg)
            }
            return l
        }

        fun replaceValue(string: String, vararg values: Any): String

        {
            var str = string
            if (values.isEmpty()) return str
            var i = 0
            while (str.matches(".*\\{[0-9]\\}.*".toRegex()))
            {
                var j : Int = if (i >= values.size)  values.size - 1 else i
                var value: String? = null
                if (i >= values.size)  j = values.size - 1
                else
                {

                    j = i

                }

                if (values[j] is String)

                {

                    value = values[j] as String

                }

                else if (values[j] is Number)

                {

                    val num = (values[i] as Number).toDouble()

                    if (num - num.toInt() == 0.0)

                    {

                        value = num.toInt().toString()

                    }

                    else

                    {

                        value = num.toString()

                    }

                }

                else if (values[j] is Boolean)

                {

                    value = (values[j] as Boolean).toString()

                }

                else

                {

                    value = values[j].toString()

                }

                str = str.replace(("\\{" + i.toString() + "\\}").toRegex(), replacement = value)

                i++

            }

            return str
        }

        fun isNumber(str: String): Boolean
        {
            return try { java.lang.Double.parseDouble(str); true }
            catch (e: NumberFormatException) { false }
        }

        fun isUniqueId(uuid: String): Boolean
        {
            return try {
                UUID.fromString(uuid)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        fun decode(escape: String): String
        {
            var escaped = escape
            if (escaped.indexOf("\\u") == -1) return escaped
            var processed = ""
            var position = escaped.indexOf("\\u")
            while (position != -1) {
                if (position != 0) processed += escaped.substring(0, position)
                val token = escaped.substring(position + 2, position + 6)
                escaped = escaped.substring(position + 6)
                processed += Integer.parseInt(token, 16).toChar()
                position = escaped.indexOf("\\u")
            }
            processed += escaped
            return processed

        }
    }
}