/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.publish_over_ssh;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.Cause.UserIdCause;

import java.io.IOException;
import java.util.ArrayList;

import jenkins.plugins.publish_over.BPInstanceConfig;
import jenkins.plugins.publish_over.BPPlugin;
import jenkins.plugins.publish_over.BPPluginDescriptor;
import jenkins.plugins.publish_over_ssh.descriptor.BapSshPublisherPluginDescriptor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;


@SuppressWarnings({ "PMD.TooManyMethods", "PMD.LooseCoupling" })
public class BapSshPublisherPlugin extends BPPlugin<BapSshPublisher, BapSshClient, BapSshCommonConfiguration> {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public BapSshPublisherPlugin(final ArrayList<BapSshPublisher> publishers, final boolean continueOnError, final boolean failOnError,
                                 final boolean alwaysPublishFromMaster, final String masterNodeName,
                                 final BapSshParamPublish paramPublish) {
        super(Messages.console_message_prefix(), publishers, continueOnError, failOnError, alwaysPublishFromMaster, masterNodeName,
                paramPublish);
    }

    public ArrayList<BapSshPublisher> getValidPublishers() {
        BPInstanceConfig delegate = super.getDelegate();
        ArrayList<BapSshPublisher> publishers = delegate.getPublishers();
        ArrayList<BapSshPublisher> list = new ArrayList<>();
        for (BapSshPublisher p : publishers) {
            String name = p.getConfigName();
            BapSshHostConfiguration conf = this.getConfiguration(name);
            if (conf.isCurrentUserOK()) {
                list.add(p);
            }
        }
        return list;
    }

    public BPInstanceConfig getValidDelegate() {
        ArrayList<BapSshPublisher> publishers = this.getValidPublishers();
        BPInstanceConfig<BapSshPublisher> conf = this.getInstanceConfig();
        return new BPInstanceConfig(publishers, conf.isContinueOnError(),
                conf.isFailOnError(), conf.isAlwaysPublishFromMaster(),
                conf.getMasterNodeName(), conf.getParamPublish());
    }

    public ArrayList<BapSshPublisher> getValidPublishersByUserID(String userId) {
        BPInstanceConfig delegate = super.getDelegate();
        ArrayList<BapSshPublisher> publishers = delegate.getPublishers();
        ArrayList<BapSshPublisher> list = new ArrayList<>();
        for (BapSshPublisher p : publishers) {
            String name = p.getConfigName();
            BapSshHostConfiguration conf = this.getConfiguration(name);
            if (conf.isUserIdOK(userId)) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        UserIdCause userIdCause = build.getCause(UserIdCause.class);
        if (userIdCause == null) {
            // For Tests
            return super.perform(build, launcher, listener);
        }

        ArrayList<BapSshPublisher> publishers = this.getValidPublishersByUserID(
                userIdCause.getUserId());
        BPInstanceConfig<BapSshPublisher> conf = this.getInstanceConfig();

        BapSshPublisherPlugin p = new BapSshPublisherPlugin(publishers,
                conf.isContinueOnError(), conf.isFailOnError(),
                conf.isAlwaysPublishFromMaster(), conf.getMasterNodeName(),
                (BapSshParamPublish) conf.getParamPublish());
        return p.run(build, launcher, listener);
    }

    private boolean run(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }

    public BapSshParamPublish getParamPublish() {
        return (BapSshParamPublish) getDelegate().getParamPublish();
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        return addToEquals(new EqualsBuilder(), (BapSshPublisherPlugin) that).isEquals();
    }

    @Override
    public int hashCode() {
        return addToHashCode(new HashCodeBuilder()).toHashCode();
    }

    @Override
    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

    @Override
    public Descriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(Descriptor.class);
    }

    public BapSshHostConfiguration getConfiguration(final String name) {
        return getDescriptor().getConfiguration(name);
    }

    @Extension
    public static class Descriptor extends BapSshPublisherPluginDescriptor {
        @Override
        public Object readResolve() {
            return super.readResolve();
        }
    }

    /** prevent xstream noise */
    @Deprecated
    public static class DescriptorMessages implements BPPluginDescriptor.BPDescriptorMessages { }

}
