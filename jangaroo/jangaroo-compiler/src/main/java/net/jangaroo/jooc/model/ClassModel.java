package net.jangaroo.jooc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A model of an ActionScript class or interface.
 */
public class ClassModel extends AbstractAnnotatedModel implements NamespacedModel {
  private boolean isInterface = false;
  private boolean isFinal = false;
  private boolean isDynamic = false;
  private String namespace = PUBLIC;
  private String superclass = null;
  private List<String> interfaces = new ArrayList<String>();
  private List<MemberModel> members = new ArrayList<MemberModel>();

  public ClassModel() {
  }

  public ClassModel(String name) {
    super(name);
  }

  public ClassModel(String name, String superclass) {
    super(name);
    this.superclass = superclass;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }

  public boolean isDynamic() {
    return isDynamic;
  }

  public void setDynamic(boolean dynamic) {
    isDynamic = dynamic;
  }

  public boolean isInterface() {
    return isInterface;
  }

  public void setInterface(boolean value) {
    this.isInterface = value;
  }

  public String getSuperclass() {
    return superclass;
  }

  public void setSuperclass(String superclass) {
    this.superclass = superclass;
  }

  public List<String> getInterfaces() {
    return Collections.unmodifiableList(interfaces);
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public void addInterface(String interfaceName) {
    interfaces.add(interfaceName);
  }

  public List<MemberModel> getMembers() {
    return Collections.unmodifiableList(members);
  }

  public void setMembers(List<MemberModel> members) {
    this.members = members;
  }

  public void addMember(MemberModel member) {
    MemberModel oldMember = getMember(member.isStatic(), member.getName());
    if (oldMember != null) {
      if (oldMember.isProperty()) {
        PropertyModel oldPropertyModel = (PropertyModel)oldMember;
        if (member.isGetter()) {
          oldMember = oldPropertyModel.getGetter();
        } else if (member.isSetter()) {
          oldMember = oldPropertyModel.getSetter();
        }
      }
      if (oldMember != null) {
        if (oldMember.equals(member)) {
          return;
        }
        throw new IllegalArgumentException("Someone tried to add a different " + (member.isStatic() ? "static " : "") + "member called " + member.getName() + " to class " + getName());
      }
    }
    if (member.isProperty()) {
      PropertyModel propertyModel = (PropertyModel)member;
      addIfNotNull(propertyModel.getGetter());
      addIfNotNull(propertyModel.getSetter());
    } else {
      members.add(member);
    }
  }

  private void addIfNotNull(MethodModel method) {
    if (method != null) {
      members.add(method);
    }
  }

  public PropertyModel getProperty(boolean isStatic, String name) {
    MemberModel member = getMember(isStatic, name);
    return member != null && member.isProperty() ? (PropertyModel)member : null;
  }

  public MemberModel getMember(String name) {
    return getMember(false, name);
  }

  public MemberModel getStaticMember(String name) {
    return getMember(true, name);
  }

  private MemberModel getMember(boolean isStatic, String name) {
    MemberModel member = getMethodOrField(isStatic, name);
    if (member != null && member.isAccessor()) {
      MethodModel counterpart = getMethod(isStatic, member.isGetter() ? MethodType.SET : MethodType.GET, name);
      return new PropertyModel((MethodModel)member, counterpart);
    }
    return member;
  }

  private MemberModel getMethodOrField(boolean isStatic, String name) {
    for (MemberModel memberModel : members) {
      if (memberModel.isStatic() == isStatic && name.equals(memberModel.getName())) {
        return memberModel;
      }
    }
    return null;
  }

  public boolean removeMember(MemberModel memberModel) {
    return members.remove(memberModel);
  }
  
  public MethodModel getConstructor() {
    return getMethod(getName());
  }

  public MethodModel getStaticMethod(String name) {
    return getStaticMethod(null, name);
  }

  public MethodModel getStaticMethod(MethodType methodType, String name) {
    return getMethod(false, methodType, name);
  }

  public MethodModel getMethod(String name) {
    return getMethod(false, name);
  }

  public MethodModel getMethod(MethodType methodType, String name) {
    return getMethod(false, methodType, name);
  }

  private MethodModel getMethod(boolean isStatic, String name) {
    return getMethod(isStatic, null, name);
  }

  private MethodModel getMethod(boolean isStatic, MethodType methodType, String name) {
    for (MemberModel memberModel : members) {
      if (memberModel.isMethod() && ((MethodModel)memberModel).getMethodType() == methodType
              && memberModel.isStatic() == isStatic && name.equals(memberModel.getName())) {
        return (MethodModel)memberModel;
      }
    }
    return null;
  }

  public MethodModel createConstructor() {
    MethodModel constructor = new MethodModel(getName(), null);
    addMember(constructor);
    return constructor;
  }

  @Override
  public void visit(ModelVisitor visitor) {
    visitor.visitClass(this);
  }

}
