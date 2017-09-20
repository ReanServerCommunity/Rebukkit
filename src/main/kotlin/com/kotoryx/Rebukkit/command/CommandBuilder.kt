package com.kotoryx.Rebukkit.command

import com.kotoryx.Rebukkit.EntityObject
import com.kotoryx.Rebukkit.Handle
import com.kotoryx.Rebukkit.RebukkitPluginBase
import com.kotoryx.Rebukkit.RebukkitUtil
import com.kotoryx.Rebukkit.command.misc.Parameter

import java.lang.reflect.ParameterizedType
import java.util.*

import com.kotoryx.Rebukkit.RebukkitUtil.Companion.color
import com.kotoryx.Rebukkit.command.misc.DetailedMessage
import com.kotoryx.Rebukkit.command.misc.Permission

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

typealias CommandType = CommandBuilder<*>
open class CommandBuilder<C : CommandBuilder<C>> : EntityObject<CommandBuilder<C>>(UUID.randomUUID().toString())
{

    @Suppress("RedundantVisibilityModifier")
    companion object
    {
        // Contains all currently registered command classes.
        // It uses this to access the command list to indirectly modify or read the information.
        @Transient private val REGISTER_COMMANDS    : MutableList<CommandType>  = ArrayList()
        @Transient public  val MAX_PAGE_SIZE        : Short                     = 7
        @Transient public  val HEADER_TITLE_MESSAGE : String                    = color("&e====&f [&b Help commands for" +
                                                                                        " &e\"{0}\" &a1/{1} &bpage(s) &f] &e====")
        @Transient public  val COMMAND_FORMAT       : String                    = "/{0} {1} : {2}"
    }

    private var handlePlugin : Handle? = null
    protected fun setHandle(handleInstance : Handle)
    {
        if(handleInstance is RebukkitPluginBase) handlePlugin = handleInstance
        else throw IllegalStateException("Handle instance must be RebukkitPluginBase Type")
    }
    fun getHandle() : Handle? = this.handlePlugin

    // This is a generic object of this class. It contains information about the superclass.
    private var genericClassType: Class<C> = (javaClass as ParameterizedType).actualTypeArguments[0] as Class<C>
    /**
     * Gets the generic type of the command.
     * @return The generic class type
     */
    fun getGenericType(): Class<C> = this.genericClassType

    /**
     * Gets an instance of this generic. It cans get the information of the upper command class.
     * @return The generic type
     * @exception InstantiationException Failed to create instance object
     * @exception IllegalAccessException Approaching an unacceptable area
     */
    @Suppress("UNCHECKED_CAST")
    fun getGenericInstance() : C
    {
        try
        {
            return this.genericClassType.newInstance()
        }
        catch(e : InstantiationException) { e.printStackTrace() }
        catch(e : IllegalAccessException) { e.printStackTrace() }
        throw RuntimeException("Cannot getting the generic instance. Please check your log.")
    }


    // Decide if you want to allow the player to use this command.
    // This value affect child commands.
    private var usableConsoleMode : Boolean   = true
    fun setEnableConsoleMode(enable: Boolean) { usableConsoleMode = enable }
    fun consoleModeEnabled() : Boolean        = usableConsoleMode

    private var usablePlayerMode  : Boolean   = true
    fun setEnablePlayerMode(enable: Boolean)  { usablePlayerMode = enable}
    fun playerModeEnabled() : Boolean         = usablePlayerMode

    private var mainCommand : String? = null
    fun isUsableCommand() : Boolean   = mainCommand.isNullOrEmpty() || mainCommand.isNullOrBlank()
    fun setMainCommand(cmd : String)  { mainCommand = cmd }
    fun getMainCommand()    : String  {
        return if(isUsableCommand()) mainCommand!!
        else throw NullPointerException("Main command not defined. Please set using setMainCommand(args)")
    }

    private var aliasCommands : MutableList<String> = ArrayList()
    fun getAliasCommand() : MutableList<String> = aliasCommands
    fun hasAliasCommand(alias : String? = null) : Boolean
    {
        return if(alias.isNullOrBlank()) aliasCommands.size != 0
        else aliasCommands.contains(alias)
    }

    private var description : MutableList<String>     = ArrayList()
    fun getCommandDescription() : MutableList<String> = description
    fun hasCommandDescription() : Boolean             = !description.isEmpty()

    private var param : Parameter? = null
    fun hasParameter() : Boolean   = param != null
    fun isAllowPermission(target: CommandSender) : Boolean = target.hasPermission(this.getPermissionValue())
    fun getParamPermission(paramRoot : Parameter = this.param!!, nodeIndex : Int = 0) : String?
    {
        if(! hasParameter()) return null
        val funcParam : (Parameter, Int) -> String? = {
            p, i -> p.getChild(i)!!.getPermission()
        }

        var permValue: String? = this.getPermissionValue()
        permValue ?: return funcParam(paramRoot, nodeIndex)
        permValue.let {
            for(k in 0..nodeIndex) permValue = "$permValue.${paramRoot.getChild(k)!!.getPermission()}"
        }
        return permValue
    }

    // The Permission for this command.
    // The Permission value of the child class is used in conjunction with the parent Permission in the parent class.
    private var permission : Permission? = null

    /**
     * Get the Permission for this class.
     * It takes the permission value of this command and is not affected by the parent class's information.
     * @return The permission value that processed by parent
     */
    fun getPermission() : Permission? = this.permission
    fun hasPermission() : Boolean     = this.permission != null
    fun getPermissionValue(target : CommandSender? = null) : String?
    {
        if(!hasPermission()) return null
        if(!hasParent())     return this.permission!!.getPermissionName(target)

        var parentCmd : CommandType? = this.parentCommand
        var perm : String = this.permission!!.name
        while(parentCmd != null)
        {
            perm = "${parentCmd.getPermission()!!.name}.$perm"
            parentCmd = parentCmd.parentCommand!!
        }
        return if(target == null) perm
        else Permission(perm, this.permission!!.defaultOP).getPermissionName(target)
    }

    /**
     * Specifies a permission value. This can be affected by the value of the parent class.<br>
     * For example, Here is the code:<br>
     * <pre>
     * <code>
     * class ParentCommand : CommandBuilder<ParentCommand>
     * {
     *   init
     *   {
     *       this.setCommand("parentcommand")
     *       this.setPermission("exampleperm")
     *       this.addChildCommand(ChildCommand())
     *   }
     * }
     * class ChildCommand : ProspaceCommand<ChildCommand>
     * {
     *   init
     *   {
     *      setCommand("childcommand")
     *      setPermission("child")
     *   }
     * }
     * ...
     * println(new ParentCommand().getChildCommand("run").getPermissionValue())
     * </code>
     * </pre>
     * The output of this code will be <code>"exampleperm.child"</code>. This shows that the value
     * can vary depending on the parent class.<br>
     * That is, the child information changes automatically according to the parent value and
     * need to enter <code>/ps run</code> to use the ChildCommand's command.<br>
     */
    protected fun setPermission(perm : Permission)
    {
        this.permission = permission
    }

    /*
     * Register the external command.
     * the external command is not extended by CommandBuilder.
     * Thus, That's simple framework type.
     */
    private var externalCommand : MutableList<CommandType> = ArrayList()
    fun addExternalCommand(command: CommandType) { this.externalCommand.add(command)}
    fun getExternalCommands() : MutableList<CommandType> = externalCommand

    // The parent class of this class.
    // This connects the commands of that class to the parent class in tree form.
    private var parentCommand : CommandType? = null
    fun hasParent() : Boolean = this.parentCommand != null
    private fun setParent(parent : CommandType) { this.parentCommand = parent }
    fun isRoot(): Boolean = this.parentCommand == null

    private var childCommand : MutableList<CommandType> = ArrayList()
    fun hasChildCommand() : Boolean = childCommand.isNotEmpty()
    fun getChildCommand(cmd: String) : CommandType?
    {
        if(hasChildCommand()) return null
        return childCommand.firstOrNull {
            it.mainCommand.equals(cmd, true) || it.hasAliasCommand(cmd)
        }
    }

    private var publicHelpPage : Boolean  = false
    fun setPublicHelp(isPublic : Boolean) { publicHelpPage = isPublic }
    fun isPublicHelp()         : Boolean  =  publicHelpPage

    fun sendHelp(sender: CommandSender)
    {
        assert()
        val commands : MutableList<CommandType> = ArrayList()
        commands.addAll(externalCommand)
        commands.addAll(childCommand)

        if(commands.size != 0)
        {
            val commandText : MutableList<DetailedMessage> = ArrayList()
            val maxPage     : Int = if(sender is ConsoleCommandSender) 1 else (commands.size / (MAX_PAGE_SIZE - 1)) + 1

            val header      : DetailedMessage = DetailedMessage(
                    RebukkitUtil.replaceValue(HEADER_TITLE_MESSAGE, this.mainCommand!!, maxPage))
            if(this.hasParameter())
            {
                var param : Parameter? = this.param
                while(param != null)
                {
                    mainCommand = "$mainCommand ${param.getParamValue(sender)}"
                    param = param.getChild()
                }
            }

            commandText.add(DetailedMessage(mainCommand!!, this.description))

            // The following process processes child commands and external commands

            // except the main command.



            // This is a function that shows one page.

            // Therefore, there is no reason to calculate the page size.

            val size_index : Int = when(sender)

            {

                is ConsoleCommandSender -> commands.size

                is Player -> if(commands.size >= MAX_PAGE_SIZE) MAX_PAGE_SIZE - 1 else commands.size

                else -> -1

            }
            for(index in 0..size_index)
            {
                val command : CommandType = commands[index]
                var relativeCommand = command.getRelativeCommand(true, sender)
                if(command.hasParameter())
                {
                    var param : Parameter = command.param!!
                    while(param.hasChild())
                    {
                        relativeCommand = "$relativeCommand ${param.getParamValue(sender)}"
                        param = param.getChild()!!
                    }
                }
                commandText.add(DetailedMessage(relativeCommand, command.description))
            }
            // Finally, messages are printing.
            for(e in commandText) e.send(sender)

        }
        else
        {
            throw NullPointerException("Not support help page.")
        }
    }

    private fun assert()
    {
        if(!isUsableCommand())
        {
            val javaClassName : String = getGenericInstance().javaClass.name
            // You must set the main command before using this command.
            // Please use the function to set the command: this.setMainCommand(String)
            throw IllegalStateException("You must set the main command before using this command")
        }
    }
}