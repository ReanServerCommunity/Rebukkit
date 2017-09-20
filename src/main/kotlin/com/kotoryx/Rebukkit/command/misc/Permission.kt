package com.kotoryx.Rebukkit.command.misc

import com.kotoryx.Rebukkit.RebukkitUtil
import com.kotoryx.Rebukkit.property.CommandProperty
import org.bukkit.command.CommandSender

class Permission(var name: String, var defaultOP: Boolean = true)
{
    fun isDefaultOP() : Boolean = defaultOP

    fun hasPermission(sender : CommandSender) : Boolean = sender.hasPermission(this.name)

    fun getPermissionName(target : CommandSender? = null) : String
    {
        target ?: return this.name
        @Suppress("UNREACHABLE_CODE")
        target.let {
            val colorSet : String = null!!
            if(target.isOp) if(this.isDefaultOP()) colorSet = CommandProperty.ALLOWED_PERM_COLORSET
            else colorSet = if(this.hasPermission(target)) CommandProperty.ALLOWED_PERM_COLORSET else CommandProperty.DEINED_PERM_COLORSET
            return RebukkitUtil.color(colorSet +
                    if(this.isDisconnected()) this.name = "$this.name.$CommandProperty.DEFAULT_CHILD_PERMISSION" else this.name)
        }
    }

    init
    {
        name = name.trimMargin()
        if(name.startsWith('.')) name = name.substring(1)
    }

    operator fun minus(str : String) : Permission
    {
        if(this.name.endsWith(".$str"))
        {
            return Permission(str.replaceAfterLast(".$str", ""), this.isDefaultOP())
        }
        throw RuntimeException("You cannot use this object to process this calculation")
    }

    operator fun plus(str : String) : Permission = Permission("$this.name.$str", this.defaultOP)

    fun  isDisconnected(): Boolean = this.name.split(".").isEmpty()

    var permissionMessage : String? = null

    fun  getMessage(): String? = this.permissionMessage
}