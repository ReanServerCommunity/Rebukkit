package com.kotoryx.Rebukkit.command.misc

import com.kotoryx.Rebukkit.RebukkitUtil
import org.bukkit.command.CommandSender

class DetailedMessage(var message : String, var desc : MutableList<String> = ArrayList())
{
    private val messageBuilder : FancyMessage        = FancyMessage(message)
    fun getMessageBuilder()    : FancyMessage        = messageBuilder
    fun getDescription()       : MutableList<String> = desc

    fun addMessage(message : String, index : Int = -1) : MutableList<String>
    {
        when (index)
        {
            -1   -> desc.add(RebukkitUtil.Companion.color(message))
            else -> desc.add(index, message)
        }
        return desc
    }

    fun send(sender : CommandSender)
    {
        messageBuilder.tooltip(desc.asIterable())
        messageBuilder.send(sender)
    }
}

/**
 * up-to date.
 */
class FancyMessage(message: String)
{
    fun tooltip(asIterable: Iterable<String>) {}
    fun send(sender: CommandSender) {}
}


