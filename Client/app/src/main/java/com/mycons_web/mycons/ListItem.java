package com.mycons_web.mycons;


public class ListItem {
	private String category;
	private String name;
	private String image;
	private String tags;
	private String id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getTags() {
        String tmpStr="";
        if(tags==null)
        {
            return tmpStr;
        }
        else {
            String[] strsTags = tags.split(",");
            for (int i = 0; i < strsTags.length; i++) {
                tmpStr += strsTags[i];
                tmpStr += " ";
            }

            tmpStr = tmpStr.replace('[', ' ');
            tmpStr = tmpStr.replace(']', ' ');
            tmpStr = tmpStr.replace('"', ' ');
            return tmpStr;
        }
	}

	public void setTags(String tags) {
		this.tags = tags;
	}


	@Override
	public String toString() {
		return "[ category=" + category + ", Name=" + name + " , tags=" + tags + "]";
	}
}
